package com.ab.hicarerun.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.ab.hicarerun.R;
import com.ab.hicarerun.databinding.AddActivityAdapterBinding;
import com.ab.hicarerun.handler.OnAddActivityClickHandler;
import com.ab.hicarerun.handler.OnDeleteListItemClickHandler;
import com.ab.hicarerun.handler.OnSelectServiceClickHandler;
import com.ab.hicarerun.viewmodel.AccountAreaViewModel;
import com.orhanobut.logger.Logger;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arjun Bhatt on 12/20/2019.
 */
public class AddActivityAdapter extends RecyclerView.Adapter<AddActivityAdapter.ViewHolder> {
    private OnAddActivityClickHandler onItemClickHandler;
    private OnSelectServiceClickHandler serviceClickHandler;
    private final Context mContext;
    private List<AccountAreaViewModel> items = null;
    List<String> serviceList = null;
    private onRadioClickChanged mRadioClickChanged;
    List<String> isCheckedList = new ArrayList<>();

    public AddActivityAdapter(Context context, List<String> serviceList, onRadioClickChanged mRadioClickChanged) {
        if (items == null) {
            items = new ArrayList<>();
        }
        this.serviceList = serviceList;
        this.mContext = context;
        this.mRadioClickChanged = mRadioClickChanged;
    }

    @NotNull
    @Override
    public AddActivityAdapter.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        AddActivityAdapterBinding mAddActivityAdapterBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.add_activity_adapter, parent, false);
        return new AddActivityAdapter.ViewHolder(mAddActivityAdapterBinding);
    }

    @Override
    public void onBindViewHolder(@NotNull final AddActivityAdapter.ViewHolder holder, final int position) {
        try {
            if (isCheckedList.isEmpty()) {
                for (int i = 0; i < serviceList.size(); i++) {
                    isCheckedList.add("false");
                }
            }

            holder.mAddActivityAdapterBinding.radioYes.setOnClickListener(view -> {
                isCheckedList.set(position, "true");
                holder.mAddActivityAdapterBinding.radioNo.setChecked(false);
                holder.mAddActivityAdapterBinding.radioYes.setChecked(true);
                serviceClickHandler.onRadioYesClicked(position);
                mRadioClickChanged.onClickChanged(position, isCheckedList);
            });
            holder.mAddActivityAdapterBinding.radioNo.setOnClickListener(view -> {
                isCheckedList.set(position, "false");
                holder.mAddActivityAdapterBinding.radioNo.setChecked(true);
                holder.mAddActivityAdapterBinding.radioYes.setChecked(false);
                serviceClickHandler.onRadioNoClicked(position);
                mRadioClickChanged.onClickChanged(position, isCheckedList);
            });

            if (holder.mAddActivityAdapterBinding.radioYes.isChecked()) {
                isCheckedList.set(position, "true");
                mRadioClickChanged.onClickChanged(position, isCheckedList);
            } else {
                isCheckedList.set(position, "false");
                mRadioClickChanged.onClickChanged(position, isCheckedList);
            }

            holder.mAddActivityAdapterBinding.txtServiceType.setText(serviceList.get(position));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setOnItemClickHandler(OnAddActivityClickHandler onItemClickHandler) {
        this.onItemClickHandler = onItemClickHandler;
    }

    public void setOnSelectServiceClickHandler(OnSelectServiceClickHandler serviceClickHandler) {
        this.serviceClickHandler = serviceClickHandler;
    }


    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    public void removeAll() {
        serviceList.removeAll(items);
        notifyDataSetChanged();
    }

    public AccountAreaViewModel getItem(int position) {
        return items.get(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final AddActivityAdapterBinding mAddActivityAdapterBinding;

        public ViewHolder(AddActivityAdapterBinding mAddActivityAdapterBinding) {
            super(mAddActivityAdapterBinding.getRoot());
            this.mAddActivityAdapterBinding = mAddActivityAdapterBinding;
        }
    }

    public interface onRadioClickChanged {
        void onClickChanged(int position, List<String> isCheckedList);
    }
}

