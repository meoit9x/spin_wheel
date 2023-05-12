package nat.pink.base.ui.home;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import nat.pink.base.R;
import nat.pink.base.base.BaseFragment;
import nat.pink.base.databinding.FragmentPrivacyBinding;

public class PrivacyFragment extends BaseFragment<FragmentPrivacyBinding, HomeViewModel> {
    private static final String TAG = "PrivacyFragment";

    @NonNull
    @Override
    public HomeViewModel getViewModel() {
        return new ViewModelProvider(this).get(HomeViewModel.class);
    }

    @Override
    public void initView() {
        super.initView();
        setTAG("PrivacyFragment");
        binding.llConstraint.setOnClickListener(v -> {});
        binding.llTop.ivBack.setOnClickListener(v -> {
            backStackFragment();
        });
        binding.llTop.ivTitle.setText(getResources().getText(R.string.privacy));
        binding.llTop.ivDone.setVisibility(View.GONE);
    }

    @Override
    public void initData() {
        super.initData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}