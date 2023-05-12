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
import androidx.annotation.RequiresApi;
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
import nat.pink.base.network.RequestAPI;
import nat.pink.base.service.ChatHeadService;
import nat.pink.base.ui.home.detail.AddEditFragment;
import nat.pink.base.ui.home.detail.ListFragment;
import nat.pink.base.ui.home.detail.ResultFragment;
import nat.pink.base.utils.Config;
import nat.pink.base.utils.Const;
import nat.pink.base.utils.PreferenceUtil;
import nat.pink.base.utils.StringUtils;
import nat.pink.base.utils.UriUtils;
import nat.pink.base.utils.Utils;
import retrofit2.Retrofit;
import rubikstudio.library.LuckyWheelView;
import rubikstudio.library.model.LuckyItem;

public class HomeFragment extends BaseFragment<HomeFragmentBinding, HomeViewModel> {

    public static final String TAG = "HomeFragment";
    private List<LuckyItem> data = new ArrayList<>();
    private final List<Integer> colors = new ArrayList<>();
    private ObjectSpin objectSpin;
    private ObjectsContentSpin objectsContentSpin;
    private List<ObjectsContentSpin> objectsContentSpins = new ArrayList<>();
    private List<ObjectsContentSpin> objectInactives = new ArrayList<>();
    private int index = 0;
    private LayoutInflater layoutInflater;
    private View viewPopup;
    private View navMenu;
    private Switch swStatus;
    private PopupWindow popupWindow;
    private DialogLoading dialogLoading;
    private boolean isStopLuckyWeel = false;
    private MediaPlayer player = new MediaPlayer();
    private String nameMusic = "";
    private boolean isVolume = true;

    @NonNull
    @Override
    public HomeViewModel getViewModel() {
        return new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
    }

    @Override
    public void initObserver() {
        super.initObserver();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void initView() {
        super.initView();
        navMenu = binding.navView.getHeaderView(0);
        swStatus = navMenu.findViewById(R.id.swStatus);
        initPopupMenu();

        binding.drawerLayout.setBackgroundColor(Color.TRANSPARENT);
        binding.lwContent.setRound(5);
        binding.lwContent.setLuckyWheelTextColor(getContext().getResources().getColor(R.color.white));
        binding.ivSpin.setOnClickListener(view -> startLucky());
        binding.ivMenu.setOnClickListener(view -> popupWindow.showAsDropDown(view));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void inactive() {
        if (objectsContentSpin == null)
            return;
        if ((objectsContentSpins.size() - objectInactives.size()) <= 2)
            return;
        if (!objectsContentSpin.getContent().equals(binding.txtContent.getText()))
            return;
        if (objectInactives.contains(objectsContentSpin))
            return;
        objectInactives.add(objectsContentSpin);
        initInActive((objectsContentSpins.size() - objectInactives.size()) <= 2);
        initActiveAll(false);
        initSpin();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initActiveAll(boolean isGone) {
        ExtTextView extTextView = viewPopup.findViewById(R.id.txt_active_all);
        extTextView.setTextColor(getContext().getColor(isGone ? R.color.color_9E9E9E : R.color.white));
        ImageFilterView imageFilterView = viewPopup.findViewById(R.id.iv_active_all);
        imageFilterView.setColorFilter(getContext().getColor(isGone ? R.color.color_9E9E9E : R.color.white));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initInActive(boolean isGone) {
        ExtTextView extTextView = viewPopup.findViewById(R.id.txt_in_active);
        extTextView.setTextColor(getContext().getColor(isGone ? R.color.color_9E9E9E : R.color.white));
        ImageFilterView imageFilterView = viewPopup.findViewById(R.id.iv_inactive);
        imageFilterView.setColorFilter(getContext().getColor(isGone ? R.color.color_9E9E9E : R.color.white));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initPopupMenu() {
        layoutInflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        viewPopup = layoutInflater.inflate(R.layout.menu_home, null, false);
        popupWindow = new PopupWindow(viewPopup);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        viewPopup.findViewById(R.id.ll_content).setOnClickListener(view -> {
            dismisDialog();
            addChildFragment(new ListFragment());
        });
        viewPopup.findViewById(R.id.ll_inactive).setOnClickListener(view -> {
            dismisDialog();
            inactive();
        });

        viewPopup.findViewById(R.id.ll_active_all).setOnClickListener(view -> {
            dismisDialog();
            if (objectInactives.size() > 0) {
                objectInactives = new ArrayList<>();
                initSpin();
                initActiveAll(true);
            }
        });

        viewPopup.findViewById(R.id.ll_setting).setOnClickListener(view -> {
            binding.drawerLayout.openDrawer(Gravity.RIGHT);
            popupWindow.dismiss();
        });
        viewPopup.findViewById(R.id.ll_parent).setOnClickListener(view -> popupWindow.dismiss());

        navMenu.findViewById(R.id.ll_privacy).setOnClickListener(v -> {
            addChildFragment(new PrivacyFragment());
            binding.drawerLayout.closeDrawers();
        });

        isVolume = PreferenceUtil.getBoolean(requireContext(), PreferenceUtil.SETTING_VOLUME, true);
        swStatus.setChecked(isVolume);
        swStatus.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            PreferenceUtil.saveBoolean(requireContext(), PreferenceUtil.SETTING_VOLUME, isChecked);
            if (player != null) {
                if (isChecked) {
                    isVolume = true;
                    player.setVolume(1, 1);
                } else {
                    isVolume = false;
                    player.setVolume(0, 0);
                }
            }
        }));
    }

    private void addChildFragment(BaseFragment fragment) {
        stopLuck();
        ((MainActivity) getActivity()).addFragment(fragment, fragment.getTAG());
    }

    private void dismisDialog() {
        if (popupWindow.isShowing())
            popupWindow.dismiss();
    }

    private void startLucky() {
        if (binding.lwContent.isRunning())
            return;
        int count = PreferenceUtil.getInt(requireContext(), PreferenceUtil.COUNT_SPIN);
        if (count != 0 && count % 3 == 0) {
//            showLoadingAds(true);
//            initAdsHome();
//            interstitialAd.loadAd();
            PreferenceUtil.saveInt(requireContext(), PreferenceUtil.COUNT_SPIN, count + 1);
            return;
        } else {
            PreferenceUtil.saveInt(requireContext(), PreferenceUtil.COUNT_SPIN, count + 1);
        }
//        showLoadingAds(false);
        playSound(requireActivity(), player, "Spin.mp3");
        isStopLuckyWeel = false;
        List<Integer> integers = filterWhileList();
        int indexChange = integers.get(new Random().nextInt(integers.size() - 1));
        binding.lwContent.startLuckyWheelWithTargetIndex(indexChange);
//        App.firebaseAnalytics.logEvent("Spin", null);

    }

    private void stopLuck() {
        if (player != null) {
            stopPlaying();
        }
        if (binding.lwContent.isRunning()) {
            isStopLuckyWeel = true;
            binding.lwContent.stopLuckWeel();
            binding.ivMenu.setEnabled(true);
            binding.txtTitle.setEnabled(true);
        }
    }

    private ArrayList<Integer> filterWhileList() {
        ArrayList<Integer> integers = new ArrayList<>();
        for (int i = 0; i < objectsContentSpins.size(); i++) {
            boolean isCheck = false;
            ObjectsContentSpin objectsContentSpin = objectsContentSpins.get(i);
            for (int j = 0; j < objectInactives.size(); j++) {
                if (objectInactives.get(j).getId() == objectsContentSpin.getId()) {
                    isCheck = true;
                    break;
                }
            }
            if (!isCheck) {
                integers.add(i);
            }
        }
        return integers;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void initData() {
        super.initData();

        dialogLoading = new DialogLoading(getContext(), R.style.MaterialDialogSheet);
        initDataColor();
        getViewModel().getDefaultSpin(requireContext());
        getViewModel().reloadData.observe(getViewLifecycleOwner(), objectSpins -> {
            if (objectSpins != null) {
                objectSpin = objectSpins;
                getViewModel().getContentSpinById(requireContext(), objectSpin.getDate());
                binding.txtTitle.setText(objectSpin.getTitle());
                binding.txtContent.setText("?");
                objectInactives = new ArrayList<>();
            }
        });
        getViewModel().reloadDataContent.observe(getViewLifecycleOwner(), objectsContentSpins -> {
            this.objectsContentSpins = objectsContentSpins;
            initSpin();
        });
    }

    @Override
    public void initEvent() {
        super.initEvent();
        binding.ivPurchase.setOnClickListener(v -> {
//            addChildFragment(new InAppFragment(o -> binding.nativeAdWordDetail.setVisibility(View.GONE)));
        });
        binding.lwContent.setOnClickListener(v -> startLucky());
        binding.lwContent.setLuckyRoundItemSelectedListener(new LuckyWheelView.LuckyRoundItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void LuckyRoundItemSelected(int index) {
                if (isStopLuckyWeel)
                    return;
                stopPlaying();
                objectsContentSpin = objectsContentSpins.get(index);
                binding.txtContent.setText(objectsContentSpin.getContent());
                binding.nativeAdWordDetail.setVisibility(View.GONE);
                Bitmap map = Utils.takeScreenShot(requireActivity(), 0);
                Bitmap fast = Utils.fastblur(map, 10);
                final Drawable draw = new BitmapDrawable(getContext().getResources(), fast);
                addChildFragment(new ResultFragment(objectsContentSpin, objectSpin, draw, o -> {
                    if (o == ResultFragment.TYPE.TYPE_INACTIVE) {
                        inactive();
                        stopPlaying();
                    }
                    if (o == ResultFragment.TYPE.TYPE_AGAIN)
                        startLucky();
                }, o -> {
                    binding.ivMenu.setEnabled(true);
                    binding.txtTitle.setEnabled(true);
                    binding.nativeAdWordDetail.setVisibility(View.VISIBLE);
                }));
            }

            @Override
            public void rotateStart(boolean isRunning) {
                playSound(requireActivity(), player, "Spin.mp3");
                binding.txtTitle.setEnabled(false);
                binding.ivMenu.setEnabled(false);
            }
        });
        binding.txtTitle.setOnClickListener(view -> {
            addChildFragment(new AddEditFragment(objectSpin));
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initDataColor() {
        colors.add(getContext().getColor(R.color.color_F4C301));
        colors.add(getContext().getColor(R.color.color_27DA09));
        colors.add(getContext().getColor(R.color.color_016CF7));
        colors.add(getContext().getColor(R.color.color_A703BA));
        colors.add(getContext().getColor(R.color.color_E50108));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initSpin() {
        data = new ArrayList<>();
        for (int i = 0; i < objectsContentSpins.size(); i++) {
            LuckyItem luckyItem = new LuckyItem();
            luckyItem.secondaryText = objectsContentSpins.get(i).getContent();
            luckyItem.color = 0;
            int finalI = i;
            objectInactives.forEach(objectsContentSpin1 -> {
                if (objectsContentSpin1.getId() == objectsContentSpins.get(finalI).getId()) {
                    luckyItem.color = requireContext().getColor(R.color.color_666666);
                }
            });
            luckyItem.color = luckyItem.color == 0 ? setColor() : luckyItem.color;
            data.add(luckyItem);
        }
        binding.lwContent.setData(data);
        binding.lwContent.setWhileList(filterWhileList());
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private Integer setColor() {
        if (!objectSpin.getTypespin().equals(Const.TYPE_SPIN_ALL)) {
            return Utils.getColorSpin(requireContext(), objectSpin.getTypespin());
        }
        if (index < colors.size() - 1 && index != -1) {
            int color = colors.get(index);
            index++;
            return color;
        }
        index = 0;
        return 0;
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLuck();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void playSound(Activity activity, MediaPlayer player, String fileName) {
        if (player == null)
            return;
        if (!StringUtils.isEmpty(nameMusic) && nameMusic.equals("Done.mp3")) {
            stopPlaying();
        }
        AssetFileDescriptor afd;
        try {
            if (!player.isPlaying()) {
                this.nameMusic = fileName;
                player = new MediaPlayer();
                afd = activity.getAssets().openFd(fileName);
                player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                player.prepare();
                player.start();
            }
            this.player = player;
            if (isVolume) {
                player.setVolume(1, 1);
            } else {
                player.setVolume(0, 0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopPlaying() {
        if (player != null) {
            player.stop();
        }
    }
}
