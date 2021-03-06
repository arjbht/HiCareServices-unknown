package com.ab.hicarerun.fragments;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ab.hicarerun.BaseApplication;
import com.ab.hicarerun.BuildConfig;
import com.ab.hicarerun.R;
import com.ab.hicarerun.activities.Camera2Activity;
import com.ab.hicarerun.adapter.ConsulationParentAdapter;
import com.ab.hicarerun.databinding.FragmentConsultaionSecondChildBinding;
import com.ab.hicarerun.databinding.FragmentInspectionSecondBinding;
import com.ab.hicarerun.handler.OnConsultationClickHandler;
import com.ab.hicarerun.handler.UserSecondConsultationHandler;
import com.ab.hicarerun.network.NetworkCallController;
import com.ab.hicarerun.network.NetworkResponseListner;
import com.ab.hicarerun.network.models.CheckListModel.UploadCheckListData;
import com.ab.hicarerun.network.models.CheckListModel.UploadCheckListRequest;
import com.ab.hicarerun.network.models.ConsulationModel.Data;
import com.ab.hicarerun.network.models.GeneralModel.GeneralData;
import com.ab.hicarerun.network.models.LoginResponse;
import com.ab.hicarerun.utils.AppUtils;
import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.realm.RealmResults;

import static android.app.Activity.RESULT_OK;
import static com.ab.hicarerun.BaseApplication.getRealm;

/**
 * A simple {@link Fragment} subclass.
 */
public class InspectionSecondFragment extends Fragment implements UserSecondConsultationHandler, OnConsultationClickHandler {
    FragmentInspectionSecondBinding mFragmentInspectionSecondBinding;
    private static final int INSPECTION_REQ = 1000;
    private static final int UPLOAD_REQ = 2000;
    private ConsulationParentAdapter mAdapter;
    private List<Data> consulationList = null;
    private HashMap<Integer, String> optionMap = new HashMap<>();
    private String selectedImagePath = "";
    private int checkPosition = 0;
    private Bitmap bitmap;
    static final int REQUEST_TAKE_PHOTO = 1;
    private File mPhotoFile;
    private List<Data> insList = null;
    private String imgURL = "";
    private List<Data> conInsList = null;
    private RealmResults<GeneralData> mTaskDetailsData = null;
    private static final int REQUEST_CODE = 1234;
    private boolean mPermissions;

    public InspectionSecondFragment() {
        // Required empty public constructor
    }

    public static InspectionSecondFragment newInstance() {
        Bundle args = new Bundle();
        InspectionSecondFragment fragment = new InspectionSecondFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppUtils.CAMERA_SCREEN = "Inspection2";
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter(AppUtils.CAMERA_SCREEN));
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String base64 = intent.getStringExtra("base64");
            uploadOnsiteImage(base64);
            Log.d("receiver", "Got message: " + base64);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mFragmentInspectionSecondBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_inspection_second, container, false);
        mFragmentInspectionSecondBinding.setHandler(this);
        return mFragmentInspectionSecondBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTaskDetailsData =
                getRealm().where(GeneralData.class).findAll();

        mFragmentInspectionSecondBinding.txtPart.setTypeface(mFragmentInspectionSecondBinding.txtPart.getTypeface(), Typeface.BOLD);
        if (AppUtils.isInspectionDone) {
            mFragmentInspectionSecondBinding.btnSave.setEnabled(true);
            mFragmentInspectionSecondBinding.btnSave.setAlpha(1f);
            mFragmentInspectionSecondBinding.btnSave.setText("OK");
        } else {
            mFragmentInspectionSecondBinding.btnSave.setEnabled(false);
            mFragmentInspectionSecondBinding.btnSave.setAlpha(0.6f);
            mFragmentInspectionSecondBinding.btnSave.setText("SAVE");
        }
        mAdapter = new ConsulationParentAdapter(getActivity(), AppUtils.isInspectionDone, "Termite", (position, option) -> {
            optionMap.put(position, option);
            consulationList.get(position).setAnswertext(option);
            mAdapter.getItem(position).setAnswerText(option);
            if (mAdapter.getItem(position).getPictureRequired()) {
                if (option.equalsIgnoreCase("No")) {
                    //consulationList.get(position).setIspicturerequired(false);
                    consulationList.get(position).setNoSelected(false);
                } else {
                    // consulationList.get(position).setIspicturerequired(true);
                    consulationList.get(position).setNoSelected(true);
                }
            }
            insList = new ArrayList<>();
            Data consModel = new Data();
            consModel.setAccountid(mAdapter.getItem(position).getAccountId());
            consModel.setId(mAdapter.getItem(position).getId());
            consModel.setAnswertext(optionMap.get(position));
            consModel.setIspicturerequired(mAdapter.getItem(position).getPictureRequired());
            consModel.setName(mAdapter.getItem(position).getName());
            consModel.setQuestiontitle(mAdapter.getItem(position).getQuestionTitle());
            consModel.setQuestioncategory(mAdapter.getItem(position).getQuestionCategory());
            consModel.setOptionlist(mAdapter.getItem(position).getOptionlists());
            consModel.setPictureUrl(imgURL);
            consModel.setQuestiontype(mAdapter.getItem(position).getQuestionType());
            consModel.setOptions(mAdapter.getItem(position).getOptions());
            consModel.setOrderid(mAdapter.getItem(position).getOrderId());
            consModel.setSrid(mAdapter.getItem(position).getSRId());
            consModel.setTaskid(mAdapter.getItem(position).getTaskId());
            insList.add(consModel);
            if (consulationList.size() == mAdapter.getItemCount()) {
                if (isListChecked(consulationList)) {
                    if (isImgChecked(consulationList)) {
                        mFragmentInspectionSecondBinding.btnSave.setEnabled(true);
                        mFragmentInspectionSecondBinding.btnSave.setAlpha(1.0f);
                    } else {
                        mFragmentInspectionSecondBinding.btnSave.setEnabled(false);
                        mFragmentInspectionSecondBinding.btnSave.setAlpha(0.6f);
                    }

                } else {
                    mFragmentInspectionSecondBinding.btnSave.setEnabled(false);
                    mFragmentInspectionSecondBinding.btnSave.setAlpha(0.6f);
                }
            } else {
                mFragmentInspectionSecondBinding.btnSave.setEnabled(false);
                mFragmentInspectionSecondBinding.btnSave.setAlpha(0.6f);
            }

        });
        mAdapter.setOnItemClickHandler(this);
        mFragmentInspectionSecondBinding.recycleView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mFragmentInspectionSecondBinding.recycleView.setHasFixedSize(true);
        mFragmentInspectionSecondBinding.recycleView.setClipToPadding(false);
        mFragmentInspectionSecondBinding.recycleView.setAdapter(mAdapter);
        mFragmentInspectionSecondBinding.txtTitle.setTypeface(mFragmentInspectionSecondBinding.txtTitle.getTypeface(), Typeface.BOLD);
        getConsulations();
    }

    private boolean isImgChecked(List<Data> inspectionList) {
        boolean isRequired = true;
        for (Data data : inspectionList) {
            if (data.isNoSelected()) {
                if (data.getPictureUrl() != null && !data.getPictureUrl().equals("") && data.isShowQuestion()) {
                    isRequired = true;
                } else {
                    isRequired = false;
                    break;
                }
            }
        }
        return isRequired;
    }

    private boolean isListChecked(List<Data> listData) {
        for (Data data : listData) {
            if (data.getAnswertext().equals("") && data.isShowQuestion()) {
                return false;
            }
        }
        return true;
    }

    private void getConsulations() {
        try {
            consulationList = new ArrayList<>();
            if (AppUtils.inspectionList != null && AppUtils.inspectionList.size() > 0) {
                consulationList = AppUtils.inspectionList;
                mAdapter.addData(AppUtils.inspectionList);
                mAdapter.notifyDataSetChanged();

                if (!AppUtils.isInspectionDone) {
                    if (consulationList.size() == mAdapter.getItemCount()) {
                        if (isListChecked(consulationList)) {
                            if (isImgChecked(consulationList)) {
                                mFragmentInspectionSecondBinding.btnSave.setEnabled(true);
                                mFragmentInspectionSecondBinding.btnSave.setAlpha(1.0f);
                            } else {
                                mFragmentInspectionSecondBinding.btnSave.setEnabled(false);
                                mFragmentInspectionSecondBinding.btnSave.setAlpha(0.6f);
                            }

                        } else {
                            mFragmentInspectionSecondBinding.btnSave.setEnabled(false);
                            mFragmentInspectionSecondBinding.btnSave.setAlpha(0.6f);
                        }
                    } else {
                        mFragmentInspectionSecondBinding.btnSave.setEnabled(false);
                        mFragmentInspectionSecondBinding.btnSave.setAlpha(0.6f);
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == RESULT_OK) {
                if (requestCode == REQUEST_TAKE_PHOTO) {
                    try {
                        selectedImagePath = mPhotoFile.getPath();
                        if (selectedImagePath != null || !selectedImagePath.equals("")) {
                            Bitmap bit = new BitmapDrawable(getResources(),
                                    selectedImagePath).getBitmap();
                            int i = (int) (bit.getHeight() * (1024.0 / bit.getWidth()));
                            bitmap = Bitmap.createScaledBitmap(bit, 1024, i, true);
                        }

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                        byte[] b = baos.toByteArray();
                        String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
                        uploadOnsiteImage(encodedImage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestStoragePermission(boolean isCamera) {
        try {
            Dexter.withActivity(getActivity())
                    .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {
                            // check if all permissions are granted
                            if (report.areAllPermissionsGranted()) {
                                if (isCamera) {
                                    dispatchTakePictureIntent();
                                }
                            }
                            // check for permanent denial of any permission
                            if (report.isAnyPermissionPermanentlyDenied()) {
                                // show alert dialog navigating to Settings
                                showSettingsDialog();
                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions,
                                                                       PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    })
                    .withErrorListener(
                            error -> Toast.makeText(getActivity(), "Error occurred! ", Toast.LENGTH_SHORT)
                                    .show())
                    .onSameThread()
                    .check();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void showSettingsDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Need Permissions");
            builder.setMessage(
                    "This app needs permission to use this feature. You can grant them in app settings.");
            builder.setPositiveButton("GOTO SETTINGS", (dialog, which) -> {
                dialog.cancel();
                openSettings();
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void openSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
            intent.setData(uri);
            startActivityForResult(intent, 101);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void dispatchTakePictureIntent() {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    // Error occurred while creating the File
                }
                if (photoFile != null) {
                    try {
                        Uri photoURI = FileProvider.getUriForFile(getActivity(),
                                BuildConfig.APPLICATION_ID + ".provider",
                                photoFile);
                        mPhotoFile = photoFile;
                        takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", Camera.CameraInfo.CAMERA_FACING_BACK);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String mFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File mFile = File.createTempFile(mFileName, ".jpg", storageDir);
        return mFile;
    }

    private void uploadOnsiteImage(String base64) {
        try {
            RealmResults<LoginResponse> LoginRealmModels =
                    BaseApplication.getRealm().where(LoginResponse.class).findAll();
            if (LoginRealmModels != null && LoginRealmModels.size() > 0) {
                String UserId = LoginRealmModels.get(0).getUserID();
                UploadCheckListRequest request = new UploadCheckListRequest();
                request.setResourceId(UserId);
                request.setFileUrl("");
                request.setFileName("");
                request.setTaskId(mTaskDetailsData.get(0).getTaskId());
                request.setFileContent(base64);

                NetworkCallController controller = new NetworkCallController();
                controller.setListner(new NetworkResponseListner<UploadCheckListData>() {
                    @Override
                    public void onResponse(int requestCode, UploadCheckListData response) {
                        try {
                            imgURL = response.getFileUrl();
                            consulationList.get(checkPosition).setPictureUrl(response.getFileUrl());
                            mAdapter.getItem(checkPosition).setPictureURL(response.getFileUrl());
                            mAdapter.notifyItemChanged(checkPosition);
                            if (isListChecked(consulationList)) {
                                if (isImgChecked(consulationList)) {
                                    mFragmentInspectionSecondBinding.btnSave.setEnabled(true);
                                    mFragmentInspectionSecondBinding.btnSave.setAlpha(1.0f);
                                } else {
                                    mFragmentInspectionSecondBinding.btnSave.setEnabled(false);
                                    mFragmentInspectionSecondBinding.btnSave.setAlpha(0.6f);
                                }
                            } else {
                                mFragmentInspectionSecondBinding.btnSave.setEnabled(false);
                                mFragmentInspectionSecondBinding.btnSave.setAlpha(0.6f);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(int requestCode) {

                    }
                });
                controller.uploadCheckListAttachment(UPLOAD_REQ, request);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onSaveButtonClicked(View view) {
        try {
            AppUtils.dataList.addAll(consulationList);
            TermiteSecondListener listener = (TermiteSecondListener) getParentFragment();
            listener.onSaveClicked();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackButtonClicked(View view) {
        TermiteSecondListener listener = (TermiteSecondListener) getParentFragment();
        listener.onBackClicked();
    }

    @Override
    public void onCameraClicked(int position) {
        checkPosition = position;
//        requestStoragePermission(true);
        init();
    }

    private void init() {
        if (mPermissions) {
            if (checkCameraHardware(getActivity())) {
                // Open the Camera
                startCamera2();
            } else {
                showSnackBar("You need a camera to use this application", Snackbar.LENGTH_INDEFINITE);
            }
        } else {
            verifyPermissions();
        }
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    private void startCamera2() {
        Intent intent = new Intent(getActivity(), Camera2Activity.class);
        intent.putExtra(AppUtils.CAMERA_ORIENTATION, "BACK");
        startActivity(intent);
    }

    private void showSnackBar(final String text, final int length) {
        View view = getActivity().findViewById(android.R.id.content).getRootView();
        Snackbar.make(view, text, length).show();
    }

    public void verifyPermissions() {
        Log.d("TAG", "verifyPermissions: asking user for permissions.");
        String[] permissions = {
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                permissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                permissions[1]) == PackageManager.PERMISSION_GRANTED) {
            mPermissions = true;
            init();
        } else {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    permissions,
                    REQUEST_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            if (mPermissions) {
                init();
            } else {
                verifyPermissions();
            }
        }
    }


    @Override
    public void onCancelImageClicked(int position) {
        try {
            consulationList.get(position).setPictureUrl(null);
            mAdapter.notifyDataSetChanged();
            if (consulationList.size() == mAdapter.getItemCount()) {
                if (isListChecked(consulationList)) {
                    if (isImgChecked(consulationList)) {
                        mFragmentInspectionSecondBinding.btnSave.setEnabled(true);
                        mFragmentInspectionSecondBinding.btnSave.setAlpha(1.0f);
                    } else {
                        mFragmentInspectionSecondBinding.btnSave.setEnabled(false);
                        mFragmentInspectionSecondBinding.btnSave.setAlpha(0.6f);
                    }

                } else {
                    mFragmentInspectionSecondBinding.btnSave.setEnabled(false);
                    mFragmentInspectionSecondBinding.btnSave.setAlpha(0.6f);
                }
            } else {
                mFragmentInspectionSecondBinding.btnSave.setEnabled(false);
                mFragmentInspectionSecondBinding.btnSave.setAlpha(0.6f);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    interface TermiteSecondListener {
        void onBackClicked();

        void onSaveClicked();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
    }
}
