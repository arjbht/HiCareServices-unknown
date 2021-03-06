package com.ab.hicarerun.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ab.hicarerun.BaseApplication;
import com.ab.hicarerun.BaseFragment;
import com.ab.hicarerun.R;
import com.ab.hicarerun.adapter.QuizCategoryAdapter;
import com.ab.hicarerun.databinding.FragmentQuizCategoryBinding;
import com.ab.hicarerun.network.NetworkCallController;
import com.ab.hicarerun.network.NetworkResponseListner;
import com.ab.hicarerun.network.models.GeneralModel.GeneralData;
import com.ab.hicarerun.network.models.LoginResponse;
import com.ab.hicarerun.network.models.QuizModel.QuizCategoryData;

import java.util.List;

import io.realm.RealmResults;

public class FragmentQuizCategory extends BaseFragment {
    FragmentQuizCategoryBinding mFragmentQuizCategoryBinding;
    private static final int QUIZ_CATEGORY = 1000;
    RecyclerView.LayoutManager layoutManager;
    private QuizCategoryAdapter mAdapter;
    RealmResults<GeneralData> mGeneralRealmModel;

    public FragmentQuizCategory() {
        // Required empty public constructor
    }

    public static FragmentQuizCategory newInstance() {
        FragmentQuizCategory fragment = new FragmentQuizCategory();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mFragmentQuizCategoryBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_quiz_category, container, false);
        getActivity().setTitle("");
        return mFragmentQuizCategoryBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFragmentQuizCategoryBinding.recycleView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        mFragmentQuizCategoryBinding.recycleView.setLayoutManager(layoutManager);
        mAdapter = new QuizCategoryAdapter(getActivity());
        mFragmentQuizCategoryBinding.recycleView.setAdapter(mAdapter);
        getQuizCategory();
        mFragmentQuizCategoryBinding.lnrWheel.setOnClickListener(view1 -> replaceFragment(SpinWheelFragment.newInstance(), "QuizFragmentCategory - SpinFragment"));
    }

    private void getQuizCategory() {
        try {
            RealmResults<LoginResponse> LoginRealmModels =
                    BaseApplication.getRealm().where(LoginResponse.class).findAll();
            if (LoginRealmModels != null && LoginRealmModels.size() > 0) {
                String resourceId = LoginRealmModels.get(0).getUserID();
                NetworkCallController controller = new NetworkCallController(this);
                controller.setListner(new NetworkResponseListner<List<QuizCategoryData>>() {

                    @Override
                    public void onResponse(int requestCode, List<QuizCategoryData> items) {
                        if (items != null && items.size() > 0) {
                            mAdapter.setData(items);
                            mAdapter.notifyDataSetChanged();
                            mAdapter.setOnItemClickHandler(position -> {
                                replaceFragment(QuizFragment.newInstance(mAdapter.getItem(position).getPuzzleId()), "QuizFragmentCategory - QuizFragment");
                                Toast.makeText(getActivity(), "Clicked", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }

                    @Override
                    public void onFailure(int requestCode) {

                    }
                });
                controller.getQuizCategory(QUIZ_CATEGORY, resourceId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}