package com.ab.hicarerun.fragments;


import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ab.hicarerun.BaseApplication;
import com.ab.hicarerun.BaseFragment;
import com.ab.hicarerun.R;
import com.ab.hicarerun.activities.TrainingActivity;
import com.ab.hicarerun.adapter.TechnicianGroomingAdapter;
import com.ab.hicarerun.adapter.VideoPlayerAdapter;
import com.ab.hicarerun.databinding.FragmentTrainingBinding;
import com.ab.hicarerun.network.NetworkCallController;
import com.ab.hicarerun.network.NetworkResponseListner;
import com.ab.hicarerun.network.models.LoginResponse;
import com.ab.hicarerun.network.models.TrainingModel.Videos;
import com.ab.hicarerun.utils.AppUtils;
import com.ab.hicarerun.utils.VerticalSpacingItemDecorator;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import io.realm.RealmResults;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class TrainingFragment extends BaseFragment {
FragmentTrainingBinding mFragmentTrainingBinding;
    private static final int VIDEO_REQUEST = 1000;
    private VideoPlayerAdapter mAdapter = null;
    private Integer pageNumber = 1;
    RecyclerView.LayoutManager layoutManager;



    public TrainingFragment() {
        // Required empty public constructor
    }

    public static TrainingFragment newInstance() {
        Bundle args = new Bundle();
        TrainingFragment fragment = new TrainingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mFragmentTrainingBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_training, container, false);
        getActivity().setTitle("Training Videos");
        return mFragmentTrainingBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        InputMethodManager imm =(InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        mFragmentTrainingBinding.swipeRefreshLayout.setOnRefreshListener(
                this::getTrainingVideos);
        mFragmentTrainingBinding.recycleView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        mFragmentTrainingBinding.recycleView.setLayoutManager(layoutManager);
        mFragmentTrainingBinding.swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_dark, android.R.color.holo_blue_light,
                android.R.color.holo_red_dark, android.R.color.holo_red_light,
                android.R.color.holo_green_dark, android.R.color.holo_green_light,
                android.R.color.holo_red_dark, android.R.color.holo_red_light);
        VerticalSpacingItemDecorator itemDecorator = new VerticalSpacingItemDecorator(2);
        mFragmentTrainingBinding.recycleView.addItemDecoration(itemDecorator);
        mAdapter = new VideoPlayerAdapter(initGlide(), getActivity());
        mFragmentTrainingBinding.recycleView.setAdapter(mAdapter);
        mFragmentTrainingBinding.recycleView.setAdapter(mAdapter);
        getTrainingVideos();
        mFragmentTrainingBinding.swipeRefreshLayout.setRefreshing(true);
    }



    private RequestManager initGlide() {
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.white_background)
                .error(R.drawable.white_background);

        return Glide.with(this)
                .setDefaultRequestOptions(options);
    }


    private void getTrainingVideos() {
        try {
            NetworkCallController controller = new NetworkCallController(this);
            controller.setListner(new NetworkResponseListner() {
                @Override
                public void onResponse(int requestCode, Object response) {
                    List<Videos> items = (List<Videos>) response;
                    mFragmentTrainingBinding.swipeRefreshLayout.setRefreshing(false);

                    if (items != null) {
                        if (pageNumber == 1 && items.size() > 0) {
                            mAdapter.setData(items);
                            mAdapter.notifyDataSetChanged();
                        } else if (items.size() > 0) {
                            mAdapter.addData(items);
                            mAdapter.notifyDataSetChanged();
                        } else {
                            pageNumber--;
                        }
                    }else {

                    }
                }

                @Override
                public void onFailure(int requestCode) {
                }
            });
            controller.getTrainingVideos(VIDEO_REQUEST);
        }catch (Exception e){
            RealmResults<LoginResponse> mLoginRealmModels = BaseApplication.getRealm().where(LoginResponse.class).findAll();
            if (mLoginRealmModels != null && mLoginRealmModels.size() > 0) {
                String userName = "TECHNICIAN NAME : "+mLoginRealmModels.get(0).getUserName();
                String lineNo = String.valueOf(new Exception().getStackTrace()[0].getLineNumber());
                String DeviceName = "DEVICE_NAME : "+ Build.DEVICE+", DEVICE_VERSION : "+ Build.VERSION.SDK_INT;
                AppUtils.sendErrorLogs(e.getMessage(), getClass().getSimpleName(), "getTrainingVideos", lineNo,userName,DeviceName);
            }
        }

    }

}
