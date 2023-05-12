package nat.pink.base.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import nat.pink.base.R;
import nat.pink.base.databinding.ItemSpinEditBinding;
import nat.pink.base.utils.Const;

public class ItemSpinThemeAdapter extends RecyclerView.Adapter<ItemSpinThemeAdapter.ViewHolder> {

    private List<String> items;
    private Context context;
    private String keySelected;

    public ItemSpinThemeAdapter(Context context, List<String> items) {
        this.context = context;
        keySelected = Const.TYPE_SPIN_ALL;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSpinEditBinding binding = ItemSpinEditBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemSpinEditBinding binding = holder.binding;
        if (items.get(position).equals(Const.TYPE_SPIN_ALL)) {
            binding.txtContent.setText(context.getString(R.string.full_color));
            binding.ivContent.setBackgroundResource(R.drawable.ic_spin_full);
        }
        if (items.get(position).equals(Const.TYPE_SPIN_G)) {
            binding.txtContent.setText(context.getString(R.string.green_color));
            binding.ivContent.setBackgroundResource(R.drawable.ic_spin_green);
        }
        if (items.get(position).equals(Const.TYPE_SPIN_B)) {
            binding.txtContent.setText(context.getString(R.string.blue_color));
            binding.ivContent.setBackgroundResource(R.drawable.ic_spin_blue);
        }
        if (items.get(position).equals(Const.TYPE_SPIN_R)) {
            binding.txtContent.setText(context.getString(R.string.red_color));
            binding.ivContent.setBackgroundResource(R.drawable.ic_spin_red);
        }
        if (items.get(position).equals(Const.TYPE_SPIN_Y)) {
            binding.txtContent.setText(context.getString(R.string.yellow_color));
            binding.ivContent.setBackgroundResource(R.drawable.ic_spin_yellow);
        }
        if (items.get(position).equals(Const.TYPE_SPIN_P)) {
            binding.txtContent.setText(context.getString(R.string.purple_color));
            binding.ivContent.setBackgroundResource(R.drawable.ic_spin_purple);
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSpinEditBinding binding;

        public ViewHolder(@NonNull ItemSpinEditBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
