package com.ab.hicarerun.activities;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.ab.hicarerun.BaseActivity;
import com.ab.hicarerun.R;
import com.ab.hicarerun.adapter.ReferralListAdapter;
import com.ab.hicarerun.adapter.VideoPlayerAdapter;
import com.ab.hicarerun.adapter.VideoPlayerRecyclerAdapter;
import com.ab.hicarerun.databinding.ActivityTrainingBinding;
import com.ab.hicarerun.fragments.TechnicianSeniorFragment;
import com.ab.hicarerun.fragments.TrainingFragment;
import com.ab.hicarerun.network.NetworkCallController;
import com.ab.hicarerun.network.NetworkResponseListner;
import com.ab.hicarerun.network.models.HandShakeModel.HandShake;
import com.ab.hicarerun.network.models.ReferralModel.ReferralList;
import com.ab.hicarerun.network.models.TrainingModel.Videos;
import com.ab.hicarerun.utils.AppUtils;
import com.ab.hicarerun.utils.MyDividerItemDecoration;
import com.ab.hicarerun.utils.VerticalSpacingItemDecorator;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;
import java.util.Objects;

public class TrainingActivity extends BaseActivity {
    ActivityTrainingBinding mActivityTrainingBinding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityTrainingBinding =
                DataBindingUtil.setContentView(this, R.layout.activity_training);
        setSupportActionBar(mActivityTrainingBinding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        addFragment(TrainingFragment.newInstance(), "TrainingActivity - TrainingFragment");
    }


    @Override
    public void onBackPressed() {
        try {
            getTrainingBack();
            super.onBackPressed();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getTrainingBack() {
        int fragment = getSupportFragmentManager().getBackStackEntryCount();
        Log.e("fragments", String.valueOf(fragment));
        if (fragment < 1) {
            finish();
        } else {
            getFragmentManager().popBackStack();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                getTrainingBack();
                break;
        }

        return true;
    }



}
