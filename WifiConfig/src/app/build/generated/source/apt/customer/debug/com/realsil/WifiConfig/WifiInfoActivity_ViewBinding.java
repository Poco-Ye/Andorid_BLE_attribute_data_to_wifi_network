// Generated code from Butter Knife. Do not modify!
package com.realsil.WifiConfig;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.ImageView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.realsil.WifiConfig.utility.RefreshableScanView;
import com.realsil.WifiConfig.view.SwipeMenuListView;
import java.lang.IllegalStateException;
import java.lang.Override;

public class WifiInfoActivity_ViewBinding implements Unbinder {
  private WifiInfoActivity target;

  @UiThread
  public WifiInfoActivity_ViewBinding(WifiInfoActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public WifiInfoActivity_ViewBinding(WifiInfoActivity target, View source) {
    this.target = target;

    target.mList = Utils.findRequiredViewAsType(source, R.id.lvWristbandDevice, "field 'mList'", SwipeMenuListView.class);
    target.refreshableView = Utils.findRequiredViewAsType(source, R.id.refreshable_view, "field 'refreshableView'", RefreshableScanView.class);
    target.mivScanBack = Utils.findRequiredViewAsType(source, R.id.ivScanBack, "field 'mivScanBack'", ImageView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    WifiInfoActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.mList = null;
    target.refreshableView = null;
    target.mivScanBack = null;
  }
}
