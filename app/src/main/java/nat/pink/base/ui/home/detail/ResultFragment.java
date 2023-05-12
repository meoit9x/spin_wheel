package nat.pink.base.ui.home.detail;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.util.Consumer;
import androidx.lifecycle.ViewModelProvider;

import nat.pink.base.base.BaseFragment;
import nat.pink.base.databinding.DialogResultSpinBinding;
import nat.pink.base.model.ObjectSpin;
import nat.pink.base.model.ObjectsContentSpin;
import nat.pink.base.ui.home.HomeViewModel;
import nat.pink.base.utils.Const;
import nat.pink.base.utils.Utils;

public class ResultFragment extends BaseFragment<DialogResultSpinBinding, HomeViewModel> {

    private Consumer<TYPE> consumer;
    private ObjectsContentSpin objectsContentSpin;
    private ObjectSpin objectSpin;
    private Drawable drawable;
    private Consumer<Object> consumerAds;

    public enum TYPE {
        TYPE_SHARE, TYPE_INACTIVE, TYPE_AGAIN
    }

    public ResultFragment(@NonNull ObjectsContentSpin objectsContentSpin, ObjectSpin objectSpin, Drawable drawable, Consumer<TYPE> consumer, Consumer<Object> consumerAds) {
        this.consumer = consumer;
        this.objectsContentSpin = objectsContentSpin;
        this.objectSpin = objectSpin;
        this.drawable = drawable;
        this.consumerAds = consumerAds;
    }

    @NonNull
    @Override
    public HomeViewModel getViewModel() {
        return new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void initView() {
        super.initView();
        setTAG("ResultFragment");
        binding.txtTitle.setText(objectSpin.getTitle());
        binding.txtContent.setText(objectsContentSpin.getContent());
        binding.llAgain.setOnClickListener(view -> {
            consumer.accept(TYPE.TYPE_AGAIN);
            backStackFragment();
        });
        binding.llShare.setOnClickListener(view -> {
            if (requireContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && requireContext().checkSelfPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
            ) {
                requireActivity().requestPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, Const.ALBUM_REQUEST_CODE
                );
                return;
            }
            if (!Utils.takeScreenshot(requireActivity(), binding.getRoot()))
                backStackFragment();
        });
        binding.llInactive.setOnClickListener(view -> {
            consumer.accept(TYPE.TYPE_INACTIVE);
            backStackFragment();
        });
        binding.getRoot().setOnClickListener(v -> {
        });
        binding.llRoot.setBackground(drawable);
    }

    @Override
    public void initData() {
        super.initData();
    }

    @Override
    public void initEvent() {
        super.initEvent();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requireContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && requireContext().checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (!Utils.takeScreenshot(requireActivity(), binding.getRoot()))
                backStackFragment();
        }

    }

    @Override
    public void onDestroy() {
        consumerAds.accept(new Object());
        super.onDestroy();
    }
}
