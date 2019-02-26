package com.realsil.WifiConfig.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.realsil.WifiConfig.R;


/**
 * @author bingshanguxue
 * @date 19/06/2017
 */

public class MessageView extends RelativeLayout {

    private ProgressBar mProgress;
    private TextView mMessageView;
    private TextView mProgressNumber;
    private String mProgressNumberFormat = "%1d/%2d";
    private static final String PROGRESS_NUMBER_FORMAT = "%1d%%";


    private CharSequence mMessage;

    private int mProgressVal;

    private boolean mHasStarted;
    private Handler mViewUpdateHandler;
    private Handler mHideHandler;


    public MessageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        View rootView = View.inflate(getContext(), R.layout.view_dfu_message, this);
        mProgress = rootView.findViewById(com.realsil.sdk.support.R.id.progress);
        mMessageView = rootView.findViewById(com.realsil.sdk.support.R.id.message);
        mProgressNumber = rootView.findViewById(com.realsil.sdk.support.R.id.progress_number);

        mViewUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                    /* Update the number and percent */
                int progress = mProgress.getProgress();
//                int max = mProgress.getMax();
//                if (mProgressNumberFormat != null) {
//                    String format = mProgressNumberFormat;
//                    mProgressNumber.setText(String.format(format, progress, max));
//                } else {
//                    mProgressNumber.setText("");
//                }
                mProgressNumber.setText(String.format(PROGRESS_NUMBER_FORMAT, progress));

//                if (mProgressPercentFormat != null) {
//                    double percent = (double) progress / (double) max;
//                    SpannableString tmp = new SpannableString(mProgressPercentFormat.format(percent));
//                    tmp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
//                            0, tmp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                    mProgressPercent.setText(tmp);
//                } else {
//                    mProgressPercent.setText("");
//                }
            }
        };

        mHideHandler = new Handler();

        if (mProgressVal > 0) {
            setProgress(mProgressVal);
        }

        if (mMessage != null) {
            setMessage(mMessage);
        }
    }

    /**
     * Change the indeterminate mode for this ProgressDialog. In indeterminate
     * mode, the progress is ignored and the dialog shows an infinite
     * animation instead.
     *
     * <p><strong>Note:</strong> A ProgressDialog with style {@link #STYLE_SPINNER}
     * is always indeterminate and will ignore this setting.</p>
     *
     * @param indeterminate true to enable indeterminate mode, false otherwise
     *
     * @see #setProgressStyle(int)
     */
    public void setIndeterminate(boolean indeterminate) {
        if (mProgress != null) {
            mProgress.setIndeterminate(indeterminate);
        }
    }

    public void setMessage(CharSequence message) {
        if (message != null) {
            mMessageView.setText(message);
            mMessageView.setVisibility(VISIBLE);

            mMessage = message;
        } else {
            mMessageView.setVisibility(GONE);
        }
    }

    public void setMessage(CharSequence message, long delayMillis) {
        setMessage(message);
        if (mHideHandler == null) {
            mHideHandler = new Handler();
        }
        mHideHandler.postDelayed(() -> setVisibility(GONE), delayMillis);
    }

    /**
     * Sets the current progress.
     *
     * @param value the current progress, a value between 0 and {@link #getMax()}
     *
     * @see ProgressBar#setProgress(int)
     */
    public void setProgress(int value) {
        mProgressVal = value;

        if (value < 0) {
            mProgress.setVisibility(GONE);
            mProgressNumber.setVisibility(GONE);
        } else {
            mProgress.setVisibility(VISIBLE);
            mProgressNumber.setVisibility(VISIBLE);
        }
        mProgress.setProgress(value);
        onProgressChanged();
    }


    private void onProgressChanged() {
        if (mViewUpdateHandler != null && !mViewUpdateHandler.hasMessages(0)) {
            mViewUpdateHandler.sendEmptyMessage(0);
        }
    }

}
