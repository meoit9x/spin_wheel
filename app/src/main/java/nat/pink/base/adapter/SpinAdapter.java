package nat.pink.base.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nat.pink.base.R;
import nat.pink.base.databinding.ItemSpinContentBinding;
import nat.pink.base.databinding.ItemTitleSpinBinding;
import nat.pink.base.model.ObjectSpinDisplay;
import nat.pink.base.ui.home.HomeViewModel;

public class SpinAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private Consumer<ObjectSpinDisplay> consumer;
    private List<ObjectSpinDisplay> mSpinSuggest = new ArrayList<>();
    private List<ObjectSpinDisplay> mYourSpin = new ArrayList<>();

    public SpinAdapter(Context context, Consumer<ObjectSpinDisplay> consumer) {
        this.context = context;
        this.consumer = consumer;
    }

    public void setObjectSpinDisplays(HashMap<String, List<ObjectSpinDisplay>> objectSpinDisplays) {
        this.mSpinSuggest = objectSpinDisplays.get(HomeViewModel.KEY_SUGGEST);
        this.mYourSpin = objectSpinDisplays.get(HomeViewModel.KEY_YOUR_SPIN);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            ItemSpinContentBinding itemBinding =
                    ItemSpinContentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolderContent(itemBinding);
        } else {
            ItemTitleSpinBinding itemTitleSpinBinding = ItemTitleSpinBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolderTitle(itemTitleSpinBinding);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolderContent) {
            ViewHolderContent viewHolderContent = (ViewHolderContent) holder;
            ItemSpinContentBinding binding = viewHolderContent.binding;
            ObjectSpinDisplay item;
            if (position <= mYourSpin.size() && mYourSpin.size() > 0) {
                item = mYourSpin.get(position - 1);
            } else {
                int index = mYourSpin.size() + (mYourSpin.size() > 0 ? 2 : 1);
                Log.e("natruou", position + " " + index);
                item = mSpinSuggest.get(position - index);
            }

            if (item == null || item.getObjectSpin() == null)
                return;

            binding.txtContent.setText(item.getContent());
            binding.txtTitle.setText(item.getObjectSpin().getTitle());
            Glide.with(context).load(item.getObjectSpin().isDefault() ? R.drawable.ic_check_spin : R.drawable.ic_un_check_spin).into(binding.ivCheck);
            ObjectSpinDisplay finalItem = item;
            binding.ivMenu.setOnClickListener(v -> {
                finalItem.setTypeSelect(ObjectSpinDisplay.TYPE_SELECT.TYPE_MENU);
                finalItem.setView(v);
                consumer.accept(finalItem);
            });
            ObjectSpinDisplay finalItem1 = item;
            binding.getRoot().setOnClickListener(v -> {
                finalItem1.setTypeSelect(ObjectSpinDisplay.TYPE_SELECT.TYPE_ONCLICK);
                consumer.accept(finalItem1);
            });
        }
        if (holder instanceof ViewHolderTitle) {
            ViewHolderTitle viewHolderTitle = (ViewHolderTitle) holder;
            viewHolderTitle.binding.txtContent.setText(context.getString(position == 0 && mYourSpin.size() != 0 ? R.string.your_content : R.string.suggest));
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (mYourSpin != null && mYourSpin.size() > 0 && mYourSpin.size() >= position) {
            if (position == 0)
                return 0;
            return 1;
        }
        if (mSpinSuggest != null && mSpinSuggest.size() > 0) {
            if (position == (mYourSpin.size() + (mYourSpin.size() > 0 ? 1 : 0)))
                return 0;
            return 1;
        }
        return 0;
    }

    @Override
    public int getItemCount() {
        int index = 0;
        if (mYourSpin != null && mYourSpin.size() > 0)
            index = mYourSpin.size() + 1;
        if (mSpinSuggest != null && mSpinSuggest.size() > 0) {
            index += mSpinSuggest.size() + 1;
        }
        return index;
    }

    public class ViewHolderContent extends RecyclerView.ViewHolder {
        private final ItemSpinContentBinding binding;

        public ViewHolderContent(@NonNull ItemSpinContentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public class ViewHolderTitle extends RecyclerView.ViewHolder {
        private final ItemTitleSpinBinding binding;

        public ViewHolderTitle(@NonNull ItemTitleSpinBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
