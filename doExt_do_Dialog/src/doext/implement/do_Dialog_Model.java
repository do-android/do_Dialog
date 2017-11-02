package doext.implement;

import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import core.DoServiceContainer;
import core.helper.DoJsonHelper;
import core.interfaces.DoIPage;
import core.interfaces.DoIScriptEngine;
import core.object.DoInvokeResult;
import core.object.DoSingletonModule;
import core.object.DoSourceFile;
import core.object.DoUIContainer;
import core.object.DoUIModule;
import doext.define.do_Dialog_IMethod;

/**
 * 自定义扩展SM组件Model实现，继承DoSingletonModule抽象类，并实现do_Dialog_IMethod接口方法；
 * #如何调用组件自定义事件？可以通过如下方法触发事件：
 * this.model.getEventCenter().fireEvent(_messageName, jsonResult);
 * 参数解释：@_messageName字符串事件名称，@jsonResult传递事件参数对象； 获取DoInvokeResult对象方式new
 * DoInvokeResult(this.getUniqueKey());
 */
public class do_Dialog_Model extends DoSingletonModule implements do_Dialog_IMethod {

	private String openData;
	private String closeData;
	private Dialog mDialog;
	private View _insertView;

	public do_Dialog_Model() throws Exception {
		super();
	}

	/**
	 * 同步方法，JS脚本调用该组件对象方法时会被调用，可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public boolean invokeSyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		if ("getData".equals(_methodName)) {
			getData(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("close".equals(_methodName)) {
			close(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("hideKeyboard".equals(_methodName)) {
			hideKeyboard(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		return super.invokeSyncMethod(_methodName, _dictParas, _scriptEngine, _invokeResult);
	}

	/**
	 * 异步方法（通常都处理些耗时操作，避免UI线程阻塞），JS脚本调用该组件对象方法时会被调用， 可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前page JS上下文环境
	 * @_callbackFuncName 回调函数名 #如何执行异步方法回调？可以通过如下方法：
	 *                    _scriptEngine.callback(_callbackFuncName,
	 *                    _invokeResult);
	 *                    参数解释：@_callbackFuncName回调函数名，@_invokeResult传递回调函数参数对象；
	 *                    获取DoInvokeResult对象方式new
	 *                    DoInvokeResult(this.getUniqueKey());
	 */
	@Override
	public boolean invokeAsyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
		if ("open".equals(_methodName)) {
			this.open(_dictParas, _scriptEngine, _callbackFuncName);
			return true;
		}
		return super.invokeAsyncMethod(_methodName, _dictParas, _scriptEngine, _callbackFuncName);
	}

	/**
	 * 关闭窗口；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void close(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		this.closeData = DoJsonHelper.getString(_dictParas, "data", "");
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
		}
	}

	/**
	 * 获取数据；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void getData(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		_invokeResult.setResultText(openData);
	}

	/**
	 * 打开窗口；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_callbackFuncName 回调函数名
	 */
	@Override
	public void open(JSONObject _dictParas, final DoIScriptEngine _scriptEngine, final String _callbackFuncName) throws Exception {
		final String _path = DoJsonHelper.getString(_dictParas, "path", "");
		String _data = DoJsonHelper.getString(_dictParas, "data", "");
		final boolean _supportClickClose = DoJsonHelper.getBoolean(_dictParas, "supportClickClose", true);
		if (TextUtils.isEmpty(_path)) {
			throw new Exception("path 不能为空!");
		}
		this.openData = _data;
		final Activity mActivity = DoServiceContainer.getPageViewFactory().getAppContext();
		final DoSourceFile _uiFile = _scriptEngine.getCurrentApp().getSourceFS().getSourceByFileName(_path);
		if (_uiFile == null)
			throw new Exception("试图打开一个无效的页面文件:" + _path);

		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					mDialog = new Dialog(mActivity, android.R.style.Theme_Holo_Dialog_NoActionBar);
					DoIPage _doPage = _scriptEngine.getCurrentPage();
					DoUIContainer _rootUIContainer = new DoUIContainer(_doPage);
					_rootUIContainer.loadFromFile(_uiFile, null, null);
					_rootUIContainer.loadDefalutScriptFile(_path);
					DoUIModule _insertViewModel = _rootUIContainer.getRootView();
					if (_insertViewModel == null) {
						throw new Exception("创建viewModel失败");
					}
					_insertView = (View) _insertViewModel.getCurrentUIModuleView();
					if (_insertView == null) {
						throw new Exception("创建view失败");
					}

					String _bgColor = _insertViewModel.getPropertyValue("bgColor");
					if (isTransparent(_bgColor)) { //判断是否是透明值
						mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
					}

					int _width = (int) _insertViewModel.getRealWidth();
					int _height = (int) _insertViewModel.getRealHeight();
					mDialog.addContentView(_insertView, new LayoutParams(_width, _height));
					mDialog.setCancelable(_supportClickClose);
					mDialog.show();

					mDialog.setOnDismissListener(new OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface arg0) {
							DoInvokeResult _result = new DoInvokeResult(getUniqueKey());
							_result.setResultText(closeData);
							_scriptEngine.callback(_callbackFuncName, _result);
							openData = "";
							closeData = "";
						}
					});

				} catch (Exception e) {
					DoServiceContainer.getLogEngine().writeError("do_Dialog open \n\t", e);
				}
			}
		});
	}

	@Override
	public void hideKeyboard(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		Activity _activity = DoServiceContainer.getPageViewFactory().getAppContext();
		InputMethodManager _imm = ((InputMethodManager) _activity.getSystemService(Context.INPUT_METHOD_SERVICE));
		if (null != mDialog) {
			View _focusView = mDialog.getCurrentFocus();
			if (null != _focusView) {
				_focusView.setFocusable(false);
				_focusView.clearFocus();
				_imm.hideSoftInputFromWindow(_focusView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
			}
		}
	}

	private boolean isTransparent(String _colorStr) {

		if (_colorStr == null || _colorStr.length() <= 0)
			return false;
		try {
			if (_colorStr.charAt(0) != '#') {
				_colorStr = "#" + _colorStr;
			}
			if (_colorStr.length() == 7) { // #ffffff
				return false;
			} else { // #ffffff88
				String alpha = _colorStr.substring(_colorStr.length() - 2, _colorStr.length());
				if ("FF".equals(alpha)) {
					return false;
				} else {
					return true;
				}
			}
		} catch (Exception ex) {
			return false;
		}
	}
}