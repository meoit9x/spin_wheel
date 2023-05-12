package nat.pink.base.ui.home;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.RemoteViews;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.utils.widget.ImageFilterView;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.util.Consumer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import nat.pink.base.MainActivity;
import nat.pink.base.R;
import nat.pink.base.base.BaseFragment;
import nat.pink.base.customView.ExtTextView;
import nat.pink.base.dao.DatabaseController;
import nat.pink.base.databinding.HomeFragmentBinding;
import nat.pink.base.dialog.DialogErrorLink;
import nat.pink.base.dialog.DialogLoading;
import nat.pink.base.dialog.DialogShowError;
import nat.pink.base.dialog.DialogShowTimer;
import nat.pink.base.model.ObjectCalling;
import nat.pink.base.model.ObjectSpin;
import nat.pink.base.model.ObjectsContentSpin;
import nat.pink.base.service.ChatHeadService;
import nat.pink.base.utils.Config;
import nat.pink.base.utils.Const;
import nat.pink.base.utils.PreferenceUtil;
import nat.pink.base.utils.StringUtils;
import nat.pink.base.utils.UriUtils;
import nat.pink.base.utils.Utils;

public class HomeFragment extends BaseFragment<HomeFragmentBinding, HomeViewModel> {
    public static final String TAG = "HomeFragment";

    @NonNull
    @Override
    public HomeViewModel getViewModel() {
        return new ViewModelProvider(getActivity()).get(HomeViewModel.class);
    }
    private boolean isMess;
    private ObjectCalling objectTop = new ObjectCalling();
    private ObjectCalling objectPopup = new ObjectCalling();
    private String edtNameFinal = "", edtMessFinal = "";
    private Context context;

    @Override
    public void initView() {
        super.initView();
        showNotification(true);
    }

    @Override
    public void initData() {
        super.initData();
    }

    @Override
    public void initEvent() {
        super.initEvent();

        binding.txtIncomingCall.setOnClickListener(v -> showNotification(true));
        binding.txtCalling.setOnClickListener(v -> showNotification(false));
        binding.edtName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                objectTop.setName(charSequence.toString());
                binding.edtError.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                edtNameFinal = editable.toString();

            }
        });
        binding.edtMess.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (isMess)
                    objectPopup.setMessage(charSequence.toString());
                else
                    objectTop.setMessage(charSequence.toString());
                binding.edtMessError.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                edtMessFinal = editable.toString();
            }
        });
        binding.txtTimer.setOnClickListener(view -> {
            DialogShowTimer dialogShowTimer = new DialogShowTimer(getContext(), R.style.MaterialDialogSheet, isMess ? objectPopup.getTimer() : objectTop.getTimer(), true, o -> {
                Utils.hiddenKeyboard(getActivity(), binding.edtName);
                if (isMess)
                    objectPopup.setTimer(o);
                else
                    objectTop.setTimer(o);
                binding.txtTimer.setText(Utils.getStringTimer(getContext(), o));
            });
            dialogShowTimer.setPickup(true, false, false);
            dialogShowTimer.show();
            dialogShowTimer.setCanceledOnTouchOutside(true);
        });
        binding.frIncomingCall.setOnClickListener(view -> {
            Utils.hiddenKeyboard(getActivity(), binding.edtName);
            Utils.openGallery(getActivity(), false);
        });
        PreferenceUtil.clearEdit(requireContext(), PreferenceUtil.KEY_CURRENT_TIME_NOTI);
        binding.layoutActionBar.txtAction.setOnClickListener(v -> {
            if (!isMess && checkError()) {
                binding.edtError.setVisibility(View.VISIBLE);
                return;
            } else if (checkMessError()) {
                binding.edtMessError.setVisibility(View.VISIBLE);
                return;
            } else if (checkErrorPath()) {
                new DialogShowError(requireContext(), R.style.MaterialDialogSheet, o -> {
                }).show();
                return;
            }

            showActionDone();
        });
    }

    private void showActionDone() {
        if (!isMess) {
            if (objectTop.getTimer() == Const.KEY_TIME_NOW) {
                if (NotificationManagerCompat.from(requireContext()).areNotificationsEnabled())
                    showNotification();
            } else {
                PreferenceUtil.saveLong(requireContext(), PreferenceUtil.KEY_CURRENT_TIME, System.currentTimeMillis() + Utils.getTimeFromKey(requireContext(), objectTop.getTimer()));
                PreferenceUtil.saveKey(requireContext(), PreferenceUtil.KEY_CURRENT_TIME_NOTI);
                Utils.startAlarmService(requireActivity(), Utils.getTimeFromKey(requireContext(), objectTop.getTimer()), Const.ACTION_SHOW_NOTI, objectTop);

            }
        } else {
            if (objectPopup.getTimer() == Const.KEY_TIME_NOW) {
                checkPermission();

            } else {
                checkPermission();
                PreferenceUtil.saveLong(requireContext(), PreferenceUtil.KEY_CURRENT_TIME, System.currentTimeMillis() + Utils.getTimeFromKey(requireContext(), objectPopup.getTimer()));
                PreferenceUtil.saveKey(requireContext(), PreferenceUtil.KEY_SHOW_POPUP);
                Utils.startAlarmService(requireActivity(), Utils.getTimeFromKey(requireContext(), objectPopup.getTimer()), Const.ACTION_SHOW_POP_UP, objectPopup);

            }
        }
    }

    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(getContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getContext().getPackageName()));
                startActivityForResult(intent, Const.ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
            } else {
                if (objectPopup.getTimer() != Const.KEY_TIME_NOW) {
                    return;
                }
                showPopup();
            }
        }
    }

    private void showPopup() {
        Gson gson = new Gson();
        String json = gson.toJson(objectPopup);
        Intent intent = new Intent(getContext(), ChatHeadService.class);
        intent.setAction(Const.ACTION_SHOW_POP_UP);
        intent.setType(json);
        requireActivity().startService(intent);
    }

    @Override
    public void onDestroy() {
        Utils.hiddenKeyboard(getActivity(), binding.edtName);
        super.onDestroy();
    }

    @Override
    public void onPause() {
        Utils.hiddenKeyboard(getActivity(), binding.edtName);
        super.onPause();
    }

    private void showNotification(boolean isShow) {
        isMess = !isShow;

        binding.txtIncomingCall.setBackgroundResource(isShow ? R.drawable.bg_button_call_on : R.drawable.bg_button_call_off);
        binding.txtCalling.setBackgroundResource(!isShow ? R.drawable.bg_button_call_on : R.drawable.bg_button_call_off);
        binding.txtIncomingCall.setTextColor(getResources().getColor(isShow ? R.color.white : R.color.color_9E9E9E));
        binding.txtCalling.setTextColor(getResources().getColor(!isShow ? R.color.white : R.color.color_9E9E9E));

        // set name
        binding.edtName.setText(objectTop.getName());

        // set mess
        binding.edtName.setVisibility(isMess ? View.GONE : View.VISIBLE);
        binding.txtTitleName.setVisibility(isMess ? View.GONE : View.VISIBLE);
        binding.edtMess.setText(isMess ? objectPopup.getMessage() : objectTop.getMessage());

        // set timer
        binding.txtTimer.setText(Utils.getStringTimer(getContext(), isMess ? objectPopup.getTimer() : objectTop.getTimer()));

        // check uri image
        Uri image = isMess ? (objectPopup.getPathImage() == null || objectPopup.getPathImage().equals("") ? null : Uri.parse(objectPopup.getPathImage()))
                : (objectTop.getPathImage() == null || objectTop.getPathImage().equals("") ? null : Uri.parse(objectTop.getPathImage()));
        if (image != null)
            Glide.with(getContext()).load(image).into(binding.ivChangePic);
        else
            Glide.with(getContext()).load(R.drawable.ic_empty_image).into(binding.ivChangePic);

        binding.layoutActionBar.txtAction.setText(getResources().getText(R.string.done));
        binding.layoutActionBar.txtTitle.setText(getResources().getText(R.string.make_notification_title));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Const.REQUEST_CODE_GALLERY) {
            Utils.openGallery(getActivity(), false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Const.ALBUM_REQUEST_CODE && data != null && data.getData() != null) {
            if (isMess)
                objectPopup.setPathImage(data.getData().toString());
            else {
                objectTop.setPathImage(data.getData().toString());
            }
            Glide.with(getContext()).load(data.getData()).into(binding.ivChangePic);
        }
        if (requestCode == Const.ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            showPopup();
        }
    }

    private boolean checkError() {
        if (isMess) {
            if (objectPopup.getName().trim().isEmpty() && edtNameFinal.trim().isEmpty()) {
                binding.edtError.setText(getResources().getText(R.string.please_fill_in_the_information));
                return true;
            } else if (objectPopup.getName().trim().length() > 25 && edtNameFinal.trim().length() > 25) {
                binding.edtError.setText(getResources().getText(R.string.maxium_characters));
                return true;
            }
        } else {
            if (objectTop.getName().trim().isEmpty() && edtNameFinal.trim().isEmpty()) {
                binding.edtError.setText(getResources().getText(R.string.please_fill_in_the_information));
                return true;
            } else if (objectTop.getName().trim().length() > 25 && edtNameFinal.trim().length() > 25) {
                binding.edtError.setText(getResources().getText(R.string.maxium_characters));
                return true;
            }
        }
        return false;
    }

    private boolean checkMessError() {
        if (isMess) {
            return objectPopup.getMessage().trim().isEmpty() && edtMessFinal.isEmpty();
        } else {
            return objectTop.getMessage().trim().isEmpty() && edtMessFinal.isEmpty();
        }
    }

    private boolean checkErrorPath() {
        if (isMess) {
            return objectPopup.getPathImage() == null;
        } else {
            return objectTop.getPathImage() == null;
        }
    }

    private void showNotification() {
        Uri imageUri = objectTop.getPathImage() == null || objectTop.getPathImage().equals("") ? null : Uri.parse(objectTop.getPathImage());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            builder.setSmallIcon(R.drawable.ic_messenger);
            builder.setColor(getResources().getColor(R.color.color_057BF7));
        } else {
            builder.setSmallIcon(R.drawable.ic_messenger);
        }

        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setAction(Const.ACTION_CREAT_NOTIFICATION);
        PendingIntent pendingIntent = PendingIntent.getActivity(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        long yourmilliseconds = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm", Locale.ENGLISH);
        Date resultdate = new Date(yourmilliseconds);

        RemoteViews contentView = new RemoteViews(requireActivity().getPackageName(), R.layout.custom_notification);
        contentView.setImageViewUri(R.id.ava, imageUri);
        contentView.setTextViewText(R.id.ext_name, objectTop.getName());
        contentView.setTextViewText(R.id.ext_title, objectTop.getMessage());
        contentView.setTextViewText(R.id.time, sdf.format(resultdate));

        builder.setContentIntent(pendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setCustomBigContentView(contentView);
        } else {
            builder.setCustomContentView(contentView);
        }
        builder.setContentTitle(getResources().getText(R.string.message));
        builder.setWhen(System.currentTimeMillis());
        builder.setAutoCancel(true);

        NotificationManager mNotificationManager =
                (NotificationManager) getContext().getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            mNotificationManager.createNotificationChannel(new NotificationChannel("my_call_app", "Call App", NotificationManager.IMPORTANCE_DEFAULT));
            builder.setChannelId("my_call_app");
        }


        // Will display the notification in the notification bar
        mNotificationManager.notify(1, builder.build());
    }
}
