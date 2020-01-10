package com.ab.hicarerun.fragments;


import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ab.hicarerun.BaseApplication;
import com.ab.hicarerun.BaseFragment;
import com.ab.hicarerun.R;
import com.ab.hicarerun.activities.OnSiteAccountDetailsActivity;
import com.ab.hicarerun.activities.OnSiteTaskActivity;
import com.ab.hicarerun.adapter.AddActivityAdapter;
import com.ab.hicarerun.adapter.ExpandableRecentAdapter;
import com.ab.hicarerun.adapter.OnSiteRecentAdapter;
import com.ab.hicarerun.adapter.OnSiteTasksAdapter;
import com.ab.hicarerun.adapter.ViewActivityAdapter;
import com.ab.hicarerun.databinding.FragmentRecentOnsiteTaskBinding;
import com.ab.hicarerun.handler.OnDeleteListItemClickHandler;
import com.ab.hicarerun.handler.OnRecentTaskClickHandler;
import com.ab.hicarerun.handler.OnSelectServiceClickHandler;
import com.ab.hicarerun.network.NetworkCallController;
import com.ab.hicarerun.network.NetworkResponseListner;
import com.ab.hicarerun.network.models.BasicResponse;
import com.ab.hicarerun.network.models.ExtendRecentModel.ParentRecent;
import com.ab.hicarerun.network.models.LoginResponse;
import com.ab.hicarerun.network.models.OnSiteModel.Account;
import com.ab.hicarerun.network.models.OnSiteModel.ActivityDetail;
import com.ab.hicarerun.network.models.OnSiteModel.OnSiteAccounts;
import com.ab.hicarerun.network.models.OnSiteModel.OnSiteArea;
import com.ab.hicarerun.network.models.OnSiteModel.OnSiteHead;
import com.ab.hicarerun.network.models.OnSiteModel.OnSiteRecent;
import com.ab.hicarerun.network.models.OnSiteModel.SaveAccountAreaResponse;
import com.ab.hicarerun.utils.AppUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import es.dmoral.toasty.Toasty;
import io.realm.RealmResults;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecentOnsiteTaskFragment extends BaseFragment implements OnRecentTaskClickHandler {
    FragmentRecentOnsiteTaskBinding mFragmentRecentOnsiteTaskBinding;
    private static final String ARG_ACCOUNT = "ARG_ACCOUNT";
    private static final int RECENT_TASKS_REQ = 1000;
    private static final int DELETE_TASKS_REQ = 2000;
    private Account model;
    private Integer pageNumber = 1;
    //    private OnSiteRecentAdapter mAdapter;
//    private RecentListAdapter mAdapter;
    private ViewActivityAdapter viewAdapter;
    RecyclerView.LayoutManager layoutManager;
    List<OnSiteHead> items = null;
    List<OnSiteRecent> SubItems = null;

    ExpandableRecentAdapter mRecentAdapter;
    List<String> expandableListTitle;
    HashMap<String, List<OnSiteRecent>> expandableListDetail;

    public RecentOnsiteTaskFragment() {
        // Required empty public constructor
    }

    public static RecentOnsiteTaskFragment newInstance(Account model) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_ACCOUNT, model);
        RecentOnsiteTaskFragment fragment = new RecentOnsiteTaskFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            model = getArguments().getParcelable(ARG_ACCOUNT);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getRecentTasks();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mFragmentRecentOnsiteTaskBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_recent_onsite_task, container, false);
        getActivity().setTitle("Activities");
        return mFragmentRecentOnsiteTaskBinding.getRoot();
    }

//    @Override
//    public void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        mAdapter.onRestoreInstanceState(savedInstanceState);
//    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFragmentRecentOnsiteTaskBinding.swipeRefreshLayout.setOnRefreshListener(
                () -> getRecentTasks());
        mFragmentRecentOnsiteTaskBinding.swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_dark, android.R.color.holo_blue_light,
                android.R.color.holo_red_dark, android.R.color.holo_red_light,
                android.R.color.holo_green_dark, android.R.color.holo_green_light,
                android.R.color.holo_red_dark, android.R.color.holo_red_light);
//        mFragmentRecentOnsiteTaskBinding.recycleView.setHasFixedSize(true);
//        layoutManager = new LinearLayoutManager(getActivity());
//        mFragmentRecentOnsiteTaskBinding.recycleView.setLayoutManager(layoutManager);
        getRecentTasks();
        mFragmentRecentOnsiteTaskBinding.swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser)
            getRecentTasks();
    }


    private void getRecentTasks() {
        try {
            if (getActivity() != null) {
                RealmResults<LoginResponse> LoginRealmModels =
                        BaseApplication.getRealm().where(LoginResponse.class).findAll();
                if (LoginRealmModels != null && LoginRealmModels.size() > 0) {
                    String userId = LoginRealmModels.get(0).getUserID();
                    NetworkCallController controller = new NetworkCallController(this);
                    controller.setListner(new NetworkResponseListner() {
                        @Override
                        public void onResponse(int requestCode, Object data) {
                            items = (List<OnSiteHead>) data;
                            mFragmentRecentOnsiteTaskBinding.swipeRefreshLayout.setRefreshing(false);
                            expandableListDetail = new HashMap<>();
                            expandableListTitle = new ArrayList<>(expandableListDetail.keySet());
                            if (items.size() > 0) {
                                mFragmentRecentOnsiteTaskBinding.emptyTask.setVisibility(View.GONE);
//                                mAdapter = new RecentListAdapter(items, getActivity());
                                for (int i = 0; i < items.size(); i++) {
                                    expandableListTitle.add(items.get(i).getHead());
                                    expandableListDetail.put(items.get(i).getHead(), items.get(i).getData());
                                    SubItems = expandableListDetail.get(expandableListTitle);
                                }
                                mRecentAdapter = new ExpandableRecentAdapter(getActivity(), expandableListTitle, expandableListDetail, mFragmentRecentOnsiteTaskBinding.expandableListView);
                                mFragmentRecentOnsiteTaskBinding.expandableListView.setAdapter(mRecentAdapter);
                                mRecentAdapter.setOnItemClickHandler(RecentOnsiteTaskFragment.this);
                                for (int i = 0; i < mFragmentRecentOnsiteTaskBinding.expandableListView.getExpandableListAdapter().getGroupCount(); i++) {
                                    //Expand group
                                    mFragmentRecentOnsiteTaskBinding.expandableListView.expandGroup(i);
                                }
//                                mFragmentRecentOnsiteTaskBinding.recycleView.setAdapter(mAdapter);
//                                if (pageNumber == 1 && items.size() > 0) {
//                                    mAdapter.setData(items);
//                                    mAdapter.notifyDataSetChanged();
//                                    mFragmentRecentOnsiteTaskBinding.emptyTask.setVisibility(View.GONE);
//                                    mFragmentRecentOnsiteTaskBinding.swipeRefreshLayout.setRefreshing(false);
//                                } else if (items.size() > 0) {
//                                    mAdapter.addData(items);
//                                    mAdapter.notifyDataSetChanged();
//                                    mFragmentRecentOnsiteTaskBinding.emptyTask.setVisibility(View.GONE);
//                                    mFragmentRecentOnsiteTaskBinding.swipeRefreshLayout.setRefreshing(false);
//                                } else {
//                                    mFragmentRecentOnsiteTaskBinding.emptyTask.setVisibility(View.VISIBLE);
//                                    mFragmentRecentOnsiteTaskBinding.swipeRefreshLayout.setRefreshing(false);
//
                            } else {
                                mFragmentRecentOnsiteTaskBinding.emptyTask.setVisibility(View.VISIBLE);
                                mFragmentRecentOnsiteTaskBinding.swipeRefreshLayout.setRefreshing(false);
                            }
                        }

                        @Override
                        public void onFailure(int requestCode) {
                            mFragmentRecentOnsiteTaskBinding.swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                    controller.getRecentAccountAreaActivity(RECENT_TASKS_REQ, model.getId(), userId, true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDeleteItemClicked(final int parent, final int child) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.ic_rubbish_bin);
        builder.setTitle("Delete Activity");
        builder.setMessage("Are you sure you want to delete this activity?")
                .setPositiveButton("No, keep it", (dialog, id) -> dialog.cancel())
                .setNegativeButton("Yes, delete it", (dialog, id) -> {
                    NetworkCallController controller = new NetworkCallController(RecentOnsiteTaskFragment.this);
                    controller.setListner(new NetworkResponseListner() {
                        @Override
                        public void onResponse(int requestCode, Object data) {
                            SaveAccountAreaResponse response = (SaveAccountAreaResponse) data;
                            if (response.getSuccess()) {
                                dialog.dismiss();
                                Toasty.success(getActivity(), "Task deleted successfully").show();
                                getRecentTasks();
                            }
                        }

                        @Override
                        public void onFailure(int requestCode) {
                            dialog.dismiss();
                        }
                    });
                    controller.getDeleteOnSiteTasks(DELETE_TASKS_REQ, expandableListDetail.get(expandableListTitle.get(parent)).get(child).getId());
                });
        AlertDialog alertdialog = builder.create();
        alertdialog.show();
//        alertDialog.setTitle("Are you sure?");
//        alertDialog.setMessage("Are you sure? you want to delete this activity.");
//        alertDialog.setIcon(R.drawable.ic_alert);
//        alertDialog.set("No, keep it", new DialogInterface.OnClickListener() {
//
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });

//        alertDialog.setButton2("Yes, delete it", new DialogInterface.OnClickListener() {
//
//            @SuppressLint("ResourceType")
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//
//                NetworkCallController controller = new NetworkCallController(RecentOnsiteTaskFragment.this);
//                controller.setListner(new NetworkResponseListner() {
//                    @Override
//                    public void onResponse(int requestCode, Object data) {
//                        SaveAccountAreaResponse response = (SaveAccountAreaResponse) data;
//                        if (response.getSuccess()) {
//                            Toasty.success(getActivity(), "Task deleted successfully").show();
//                            getRecentTasks();
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(int requestCode) {
//
//                    }
//                });
//                controller.getDeleteOnSiteTasks(DELETE_TASKS_REQ, items.get(position).getId());
//            }
//        });
    }

    @Override
    public void onViewItemClicked(int parent, int child) {
        viewTaskDetailsDialog(parent, child);
    }

    private void viewTaskDetailsDialog(int parent, int child) {
        try {
            LayoutInflater li = LayoutInflater.from(getActivity());
            View promptsView = li.inflate(R.layout.layout_add_activity_dialog, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setView(promptsView);
            final AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setCancelable(false);
            final RecyclerView recyclerView =
                    (RecyclerView) promptsView.findViewById(R.id.recycleView);
            final Button btnDone =
                    (Button) promptsView.findViewById(R.id.btnDone);
            final Button btnCancel =
                    (Button) promptsView.findViewById(R.id.btnCancel);
            final TextView txtTitle =
                    (TextView) promptsView.findViewById(R.id.txtTitle);

            recyclerView.setHasFixedSize(true);
            layoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(layoutManager);
            viewAdapter = new ViewActivityAdapter(getActivity(), expandableListDetail.get(expandableListTitle.get(parent)).get(child).getActivityDetail());
            txtTitle.setText(expandableListDetail.get(expandableListTitle.get(parent)).get(child).getAreaSubType()+" Activity");
            recyclerView.setAdapter(viewAdapter);
            btnDone.setVisibility(View.GONE);
            btnCancel.setText("OK");
            btnCancel.setOnClickListener(view -> alertDialog.cancel());
            alertDialog.setIcon(R.mipmap.logo);
            alertDialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(int parent, int child) {

    }
}
