package io.github.skepter.devrantwear;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.DelayedConfirmationView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ActionFragment extends Fragment implements View.OnClickListener, DelayedConfirmationView.DelayedConfirmationListener {

    private static Listener mListener;
    private DelayedConfirmationView vIcon;
    private TextView vLabel;

    public static ActionFragment create(int iconResId, int labelResId, Listener listener) {
        mListener = listener;
        ActionFragment fragment = new ActionFragment();
        Bundle args = new Bundle();
        args.putInt("ICON", iconResId);
        args.putInt("LABEL", labelResId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_action, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vIcon = (DelayedConfirmationView) view.findViewById(R.id.icon);
        vLabel = (TextView) view.findViewById(R.id.label);
        vIcon.setImageResource(getArguments().getInt("ICON"));
        vLabel.setText(getArguments().getInt("LABEL"));

        vIcon.setListener(this);
        view.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Log.d("ActionFragment", "Timer started!");
        vIcon.setTotalTimeMs(1000);
        vIcon.start();
        mListener.onActionPerformed();
    }

    @Override
    public void onTimerFinished(View view) {
        Log.d("ActionFragment", "Timer finished!");
        vIcon.setTotalTimeMs(1000);
        vIcon.start();
    }

    @Override
    public void onTimerSelected(View view) {
        Log.d("ActionFragment", "Timer cancelled (pressed again)");
    }

    public interface Listener {
        public void onActionPerformed();
    }
}