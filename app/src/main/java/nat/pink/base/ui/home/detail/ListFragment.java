package nat.pink.base.ui.home.detail;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import nat.pink.base.MainActivity;
import nat.pink.base.R;
import nat.pink.base.adapter.SpinAdapter;
import nat.pink.base.base.BaseFragment;
import nat.pink.base.databinding.FragmentDetailBinding;
import nat.pink.base.model.ObjectSpinDisplay;
import nat.pink.base.ui.home.HomeViewModel;

public class ListFragment extends BaseFragment<FragmentDetailBinding, HomeViewModel> {

    private LayoutInflater layoutInflater;
    private View viewPopup;
    PopupWindow popupWindow;
    private SpinAdapter spinAdapter;
    private ObjectSpinDisplay objectSpinDisplay = new ObjectSpinDisplay();

    @NonNull
    @Override
    public HomeViewModel getViewModel() {
        return new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void initView() {
        super.initView();
        setTAG("ListFragment");
        binding.actionBar.txtTitle.setText(getString(R.string.content));
        binding.actionBar.txtAction.setText(getString(R.string.add));
        binding.rcvContent.setLayoutManager(new LinearLayoutManager(requireContext()));
        spinAdapter = new SpinAdapter(requireContext(), objectSpinDisplay -> {
            this.objectSpinDisplay = objectSpinDisplay;
            if (objectSpinDisplay.getTypeSelect() == ObjectSpinDisplay.TYPE_SELECT.TYPE_MENU) {
                dismisDialog();
                viewPopup.findViewById(R.id.ll_delete).setVisibility(objectSpinDisplay.getObjectSpin().isDefault() ? View.GONE : View.VISIBLE);
                popupWindow.showAsDropDown(objectSpinDisplay.getView());
            }
            if (objectSpinDisplay.getTypeSelect() == ObjectSpinDisplay.TYPE_SELECT.TYPE_ONCLICK) {
                if (objectSpinDisplay.getObjectSpin().isDefault())
                    return;
                getViewModel().updateIsDefault(requireContext(), objectSpinDisplay.getObjectSpin().getDate());
            }
        });
        binding.rcvContent.setAdapter(spinAdapter);
        initPopupMenu();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void initData() {
        super.initData();
        getViewModel().getSpinAndContent(requireContext());
        getViewModel().reloadDataListSpin.observe(getViewLifecycleOwner(), objectSpinDisplays -> {
            spinAdapter.setObjectSpinDisplays(objectSpinDisplays);
        });
    }

    @Override
    public void initEvent() {
        super.initEvent();
        binding.actionBar.ivBack.setOnClickListener(v -> backStackFragment());
        binding.llRoot.setOnClickListener(view -> {
        });
        binding.actionBar.txtAction.setOnClickListener(view -> ((MainActivity) getActivity()).addFragment(new AddEditFragment(), AddEditFragment.class.getSimpleName()));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initPopupMenu() {
        layoutInflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        viewPopup = layoutInflater.inflate(R.layout.menu_list_spin, null, false);
        popupWindow = new PopupWindow(viewPopup);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        viewPopup.findViewById(R.id.ll_edit).setOnClickListener(view -> {
            ((MainActivity) getActivity()).addFragment(new AddEditFragment(objectSpinDisplay.getObjectSpin()), AddEditFragment.class.getSimpleName());
            dismisDialog();
        });
        viewPopup.findViewById(R.id.ll_delete).setOnClickListener(view -> {
            getViewModel().deleteContentSpin(requireContext(), objectSpinDisplay.getObjectSpin());
            spinAdapter.notifyDataSetChanged();
            dismisDialog();
        });
    }

    private void dismisDialog() {
        if (popupWindow.isShowing())
            popupWindow.dismiss();
    }
}
