package com.ab.hicarerun.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.ab.hicarerun.R;
import com.ab.hicarerun.databinding.ChemicalRecycleRowBinding;
import com.ab.hicarerun.databinding.LayoutChemicalHeaderBinding;
import com.ab.hicarerun.handler.OnListItemClickHandler;
import com.ab.hicarerun.network.models.ChemicalModel.Chemicals;
import com.ab.hicarerun.network.models.ChemicalModel.MSTChemicals;
import com.ab.hicarerun.network.models.GeneralModel.GeneralData;
import com.ab.hicarerun.viewmodel.ChemicalMSTViewModel;
import com.ab.hicarerun.viewmodel.ChemicalViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.realm.RealmResults;

import static com.ab.hicarerun.BaseApplication.getRealm;

/**
 * Created by Arjun Bhatt on 1/7/2020.
 */
public class ChemicalMSTAdapter extends RecyclerView.Adapter<ChemicalMSTAdapter.ViewHolder> {

    private OnListItemClickHandler onItemClickHandler;
    private final Context mContext;
    private static List<ChemicalViewModel> items = null;
    private static HashMap<Integer, String> map = new HashMap();
    private Boolean isVerified = false;
    private OnEditTextChanged onEditTextChanged;
    private int ChemicalNo = 0;
    private int standardChem = 0;


    public ChemicalMSTAdapter(Context context, OnEditTextChanged onEditTextChanged) {
        if (items == null) {
            items = new ArrayList<>();
        }
        this.mContext = context;
        this.onEditTextChanged = onEditTextChanged;
    }


    @Override
    public ChemicalMSTAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ChemicalRecycleRowBinding mChemicalRecycleRowBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.chemical_recycle_row, parent, false);
        return new ChemicalMSTAdapter.ViewHolder(mChemicalRecycleRowBinding);
    }

    @Override
    public void onBindViewHolder(final ChemicalMSTAdapter.ViewHolder holder, final int position) {
        try {
            final ChemicalViewModel model = items.get(position);
            holder.mChemicalRecycleRowBinding.chemName.setText(model.getName());
            holder.mChemicalRecycleRowBinding.chemConsumption.setText(model.getConsumption());
            holder.mChemicalRecycleRowBinding.chemStandard.setText(model.getStandard());
            RealmResults<GeneralData> mGeneralRealmData =
                    getRealm().where(GeneralData.class).findAll();
            if (mGeneralRealmData != null && mGeneralRealmData.size() > 0) {
                isVerified = mGeneralRealmData.get(0).getAutoSubmitChemicals();
                String status = mGeneralRealmData.get(0).getSchedulingStatus();

                if (isVerified || status.equals("Completed")) {
                    holder.mChemicalRecycleRowBinding.edtActual.setText(model.getEdtActual());
                    holder.mChemicalRecycleRowBinding.edtActual.setTextColor(Color.parseColor("#000000"));
                    holder.mChemicalRecycleRowBinding.edtActual.setEnabled(false);
                } else if (status.equals("Incomplete")) {
                    holder.mChemicalRecycleRowBinding.edtActual.setText("-");
                    holder.mChemicalRecycleRowBinding.edtActual.setTextColor(Color.parseColor("#808080"));
                    holder.mChemicalRecycleRowBinding.edtActual.setEnabled(false);
                } else if (status.equals("Dispatched")) {
                    holder.mChemicalRecycleRowBinding.edtActual.setEnabled(false);
                    holder.mChemicalRecycleRowBinding.edtActual.setBackgroundResource(R.drawable.disable_edit_borders);
                } else {
                    holder.mChemicalRecycleRowBinding.edtActual.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            try {
                                onEditTextChanged.onTextChanged(position, s.toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public int getItemCount() {
        return items.size();

    }

    public void setOnItemClickHandler(OnListItemClickHandler onItemClickHandler) {
        this.onItemClickHandler = onItemClickHandler;
    }


    public void setData(List<Chemicals> data) {
        items.clear();
        for (int index = 0; index < data.size(); index++) {
            ChemicalViewModel chemicalViewModel = new ChemicalViewModel();
            chemicalViewModel.clone(data.get(index));
            items.add(chemicalViewModel);
        }
    }

    public void addData(List<Chemicals> data) {
        for (int index = 0; index < data.size(); index++) {
            ChemicalViewModel chemicalViewModel = new ChemicalViewModel();
            chemicalViewModel.clone(data.get(index));
            items.add(chemicalViewModel);
        }
    }

    public ChemicalViewModel getItem(int position) {
        return items.get(position);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private final ChemicalRecycleRowBinding mChemicalRecycleRowBinding;

        public ViewHolder(ChemicalRecycleRowBinding mChemicalRecycleRowBinding) {
            super(mChemicalRecycleRowBinding.getRoot());
            this.mChemicalRecycleRowBinding = mChemicalRecycleRowBinding;
        }
    }

    public interface OnEditTextChanged {
        void onTextChanged(int position, String charSeq);
    }
}