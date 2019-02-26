// Generated code from Butter Knife. Do not modify!
package com.realsil.WifiConfig;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.realsil.WifiConfig.utility.RefreshableScanView;
import com.realsil.WifiConfig.view.SwipeMenuListView;
import java.lang.IllegalStateException;
import java.lang.Override;

public class WifiConfigHomeActivity_ViewBinding implements Unbinder {
  private WifiConfigHomeActivity target;

  @UiThread
  public WifiConfigHomeActivity_ViewBinding(WifiConfigHomeActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public WifiConfigHomeActivity_ViewBinding(WifiConfigHomeActivity target, View source) {
    this.target = target;

    target.mList = Utils.findRequiredViewAsType(source, R.id.lvWristbandDevice, "field 'mList'", SwipeMenuListView.class);
    target.refreshableView = Utils.findRequiredViewAsType(source, R.id.refreshable_view, "field 'refreshableView'", RefreshableScanView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    WifiConfigHomeActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.mList = null;
    target.refreshableView = null;
  }
}
