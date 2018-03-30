package com.project.ken.vecurityguard;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by ken on 3/5/18.
 */

public class BottomSheetOwnerFragment extends BottomSheetDialogFragment{
    String mTag;
    String mOwnerName;
    String mCarName;
    String mLicenseNumber;
    String mPeriod;
    String mCost;
    String mAvatar;
    String mCarImage;

    public static BottomSheetOwnerFragment newInstance (String tag){
        BottomSheetOwnerFragment f = new BottomSheetOwnerFragment();
        Bundle args = new Bundle();
        args.putString("TAG",tag);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTag = getArguments().getString("TAG");
        mOwnerName = getArguments().getString("owner_name");
        mCarName = getArguments().getString("car_name");
        mLicenseNumber = getArguments().getString("license_number");
        mPeriod = getArguments().getString("duration");
        mCost = getArguments().getString("total_cost");
        mAvatar = getArguments().getString("avatar");
        mCarImage = getArguments().getString("car_image");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_owner,container,false);
        TextView ownerName = view.findViewById(R.id.owner_name);
        TextView carName = view.findViewById(R.id.car_name);
        TextView licenseNumber = view.findViewById(R.id.license_number);
        TextView period = view.findViewById(R.id.period);
        TextView cost = view.findViewById(R.id.cost);
        ImageView avatar = view.findViewById(R.id.avatar);
        ImageView carImage = view.findViewById(R.id.car_image);

        ownerName.setText(mOwnerName);
        carName.setText(mCarName);
        licenseNumber.setText(mLicenseNumber);

        double doubleDuration = Double.parseDouble(mPeriod);
        int duration = (int) doubleDuration;

        int hours = duration / 60; //since both are ints, you get an int
        int minutes = duration % 60;

        period.setText("Guarding time "+hours+"hrs : "+minutes+"mins");
        cost.setText("UGX "+mCost);

        if (mAvatar != null
                && !TextUtils.isEmpty(mAvatar)) {
            Picasso.with(getActivity())
                    .load(mAvatar)
                    .into(avatar);
        }

        if (mCarImage != null
                && !TextUtils.isEmpty(mCarImage)) {
            Picasso.with(getActivity())
                    .load(mCarImage)
                    .into(carImage);
        }


        //Manipulate view data
        return  view;

    }
}
