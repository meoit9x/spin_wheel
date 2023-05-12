package nat.pink.base.ui.home.detail;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;

import java.util.ArrayList;
import java.util.List;

import nat.pink.base.R;
import nat.pink.base.adapter.ItemContentAdapter;
import nat.pink.base.base.BaseFragment;
import nat.pink.base.databinding.FragmentAddEditBinding;
import nat.pink.base.dialog.DialogConfirm;
import nat.pink.base.dialog.DialogNameContent;
import nat.pink.base.dialog.ItemSpinThemeAdapter;
import nat.pink.base.model.ObjectSpin;
import nat.pink.base.model.ObjectsContentSpin;
import nat.pink.base.ui.home.HomeViewModel;
import nat.pink.base.utils.Const;
import nat.pink.base.utils.Utils;

public class AddEditFragment extends BaseFragment<FragmentAddEditBinding, HomeViewModel> {

    private ObjectSpin objectSpin = new ObjectSpin();
    private ItemContentAdapter itemContentAdapter = null;

    private List<ObjectsContentSpin> objectDefaults = new ArrayList<>();
    private long dateId = 0;
    private List<String> strings = new ArrayList<>();

    public AddEditFragment(ObjectSpin objectSpin) {
        this.objectSpin = objectSpin;
    }

    public AddEditFragment() {
    }

    @NonNull
    @Override
    public HomeViewModel getViewModel() {
        return new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
    }

    @Override
    public void initView() {
        super.initView();
        setTAG("AddEditFragment");
        binding.viewPagerImageSlider.setClipToPadding(false);
        binding.viewPagerImageSlider.setClipChildren(false);
        binding.viewPagerImageSlider.setOffscreenPageLimit(3);
        binding.viewPagerImageSlider.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        compositePageTransformer.addTransformer((page, position) -> {
            float r = 1 - Math.abs(position);
            page.setScaleY(0.85f + r * 0.15f);
        });
        binding.viewPagerImageSlider.setPageTransformer(compositePageTransformer);
        binding.rcvContent.setLayoutManager(new LinearLayoutManager(requireContext()));
        itemContentAdapter = new ItemContentAdapter(requireContext(), object_item_content -> {
            if (object_item_content.getType_action() == ItemContentAdapter.TYPE_ACTION.EDIT) {
                new DialogNameContent(requireActivity(), R.style.MaterialDialogSheet, getDrawableLayout(), objectDefaults.get(object_item_content.getIndex()).getContent(), o -> {
                    objectDefaults.get(object_item_content.getIndex()).setContent(o);
                    itemContentAdapter.setItems(objectDefaults);
                    hideListView();
                }).show();
            }
            if (object_item_content.getType_action() == ItemContentAdapter.TYPE_ACTION.DELETE) {
                new DialogConfirm(requireActivity(), R.style.MaterialDialogSheet, getDrawableLayout(), DialogConfirm.TYPE_DIALOG.TYPE_DELETE, type_dialog -> {
                    objectDefaults.remove(object_item_content.getIndex());
                    itemContentAdapter.setItems(objectDefaults);
                    hideListView();
                }).show();
            }
        });
        binding.rcvContent.setAdapter(itemContentAdapter);
        binding.llRoot.setOnClickListener(view -> {
        });
        binding.ivEdit.setOnClickListener(v -> {
            binding.txtContent.setText("");
        });
        binding.txtContent.setOnKeyListener((v, keyCode, event) -> {
            switch (keyCode) {
                case KeyEvent.KEYCODE_ENTER:
                    return true;
            }
            return false;
        });
    }

    @Override
    public void initData() {
        super.initData();
        strings.add(Const.TYPE_SPIN_ALL);
        strings.add(Const.TYPE_SPIN_G);
        strings.add(Const.TYPE_SPIN_R);
        strings.add(Const.TYPE_SPIN_B);
        strings.add(Const.TYPE_SPIN_P);
        strings.add(Const.TYPE_SPIN_Y);
        binding.viewPagerImageSlider.setAdapter(new ItemSpinThemeAdapter(requireContext(), strings));

        // edit spin
        if (objectSpin.getDate() != 0) {
            dateId = objectSpin.getDate();
            getViewModel().getContentSpinById(requireContext(), objectSpin.getDate());
            getViewModel().reloadDataContent.observe(getViewLifecycleOwner(), objectsContentSpins -> {
                objectDefaults = objectsContentSpins;
                itemContentAdapter.setItems(objectsContentSpins);
                hideListView();
            });
            getViewModel().reloadData.observe(getViewLifecycleOwner(), objectSpin1 -> {
                if (objectSpin == null)
                    this.objectSpin = objectSpin1;
                binding.viewPagerImageSlider.setCurrentItem(strings.indexOf(objectSpin.getTypespin()), false);
                binding.txtContent.setText(objectSpin.getTitle());
            });
        } else {
            dateId = System.currentTimeMillis();
        }

        binding.actionBar.txtAction.setText(getString(R.string.save));
        binding.actionBar.txtTitle.setText(getString(objectSpin.getDate() != 0 ? R.string.edit : R.string.add));
        hideListView();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void initEvent() {
        super.initEvent();
        binding.actionBar.ivBack.setOnClickListener(view -> backStackFragment());
        binding.txtSpinTheme.setOnClickListener(view -> binding.viewPagerImageSlider.setVisibility(binding.viewPagerImageSlider.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE));
        binding.actionBar.txtAction.setOnClickListener(view -> backStackFragment());
        binding.txtContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.txtCount.setText("(" + (24 - charSequence.length()) + ")");
            }

            @Override
            public void afterTextChanged(Editable editable) {
                binding.ivEdit.setImageResource("".equals(editable.toString()) ? R.drawable.ic_edit_pen : R.drawable.ic_check_done);
                binding.ivEdit.setBackgroundResource("".equals(editable.toString()) ? R.drawable.bg_white : R.drawable.bg_iv_check_done);
            }
        });
        binding.txtAddContent.setOnClickListener(view -> new DialogNameContent(requireActivity(), R.style.MaterialDialogSheet, getDrawableLayout(), "", getObjectDefaults(), o -> {
            ObjectsContentSpin objectsContentSpin = new ObjectsContentSpin();
            objectsContentSpin.setContent(o);
            objectsContentSpin.setDate(dateId);
            objectsContentSpin.setId(System.currentTimeMillis());
            objectDefaults.add(0, objectsContentSpin);
            itemContentAdapter.setItems(objectDefaults);
            setObjectDefaults(objectDefaults);
            hideListView();
        }).show());
        binding.actionBar.txtAction.setOnClickListener(view -> {
            if ("".equals(binding.txtContent.getText().toString().trim())) {
                new DialogConfirm(requireActivity(), R.style.MaterialDialogSheet, getDrawableLayout(), DialogConfirm.TYPE_DIALOG.TYPE_ERROR_EMPTY_QUESTION, type_dialog -> {
                }).show();
                return;
            }
            if (objectDefaults.size() <= 4) {
                new DialogConfirm(requireActivity(), R.style.MaterialDialogSheet, getDrawableLayout(), DialogConfirm.TYPE_DIALOG.TYPE_ERROR_MIN_CONTENT, type_dialog -> {
                }).show();
                return;
            }
            if (objectDefaults.size() > 16) {
                new DialogConfirm(requireActivity(), R.style.MaterialDialogSheet, getDrawableLayout(), DialogConfirm.TYPE_DIALOG.TYPE_ERROR_MAX_CONTENT_IN_APP, type_dialog -> {
//                    ((MainActivity) getActivity()).addChildFragment(new InAppFragment(o -> {
//                        setHideAds(true);
//                    }), InAppFragment.class.getSimpleName());
                }).show();
                return;
            }
            if (objectDefaults.size() > 10) {
                new DialogConfirm(requireActivity(), R.style.MaterialDialogSheet, getDrawableLayout(), DialogConfirm.TYPE_DIALOG.TYPE_ERROR_MAX_CONTENT, type_dialog -> {
                }).show();
                return;
            }
            if (objectSpin.getDate() != 0) {
                objectSpin.setTitle(binding.txtContent.getText().toString().trim());
                objectSpin.setTypespin(strings.get(binding.viewPagerImageSlider.getCurrentItem()));
                getViewModel().updateSpin(requireContext(), objectSpin, objectDefaults);
                backStackFragment();
            } else {
                objectSpin = new ObjectSpin();
                objectSpin.setTitle(binding.txtContent.getText().toString().trim());
                objectSpin.setTypespin(strings.get(binding.viewPagerImageSlider.getCurrentItem()));
                objectSpin.setSuggest(false);
                objectSpin.setDefault(false);
                objectSpin.setDate(dateId);
                getViewModel().insertSpin(requireContext(), objectSpin, objectDefaults);
                backStackFragment();
            }
        });
        binding.ivEdit.setOnClickListener(view -> {
            if ("".equals(binding.txtContent.getText().toString())) {
                binding.txtContent.requestFocus();
            } else {
                Utils.hiddenKeyboard(requireActivity(), binding.txtContent);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void hideListView() {
        binding.rcvContent.setVisibility(itemContentAdapter.getItemCount() > 0 ? View.VISIBLE : View.GONE);
        binding.txtEmptyView.setVisibility(itemContentAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    private Drawable getDrawableLayout() {
        Bitmap map = Utils.takeScreenShot(requireActivity(), 100);
        Bitmap fast = Utils.fastblur(map, 10);
        return new BitmapDrawable(getContext().getResources(), fast);
    }

    public List<ObjectsContentSpin> getObjectDefaults() {
        return objectDefaults;
    }

    public void setObjectDefaults(List<ObjectsContentSpin> objectDefaults) {
        this.objectDefaults = objectDefaults;
    }
}
