package io.github.skepter.devrantwear;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * Created by Jorel on 26/01/2017.
 */

public class ImageFragment extends Fragment {

    public static ImageFragment create(byte[] img) {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        if(img == null) {
            args.putBoolean("NO_IMAGE", true);
        } else {
            args.putBoolean("NO_IMAGE", false);
        }
        args.putByteArray("IMG", img);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_image, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        ImageView v = (ImageView) view.findViewById(R.id.imageView);

        if(getArguments().getBoolean("NO_IMAGE")) {
            return;
        }

        byte[] imgRaw = getArguments().getByteArray("IMG");
        Bitmap image = null;
        try {
            if(imgRaw.length != 0) {
                image = BitmapFactory.decodeByteArray(imgRaw,0,imgRaw.length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        v.setImageBitmap(image);
    }
}
