package nat.pink.base.adapter;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.function.Consumer;

import nat.pink.base.databinding.ItemContentSpinBinding;
import nat.pink.base.model.ObjectsContentSpin;

public class ItemContentAdapter extends RecyclerView.Adapter<ItemContentAdapter.ViewHolder> {

    private Context context;
    private List<ObjectsContentSpin> items;

    public enum TYPE_ACTION {
        DELETE, EDIT
    }

    private Consumer<OBJECT_ITEM_CONTENT> consumer;

    public ItemContentAdapter(Context context, Consumer<OBJECT_ITEM_CONTENT> consumer) {
        this.context = context;
        this.consumer = consumer;
    }

    public void setItems(List<ObjectsContentSpin> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContentSpinBinding binding = ItemContentSpinBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemContentSpinBinding binding = holder.binding;
        ObjectsContentSpin objectsContentSpin = items.get(position);
        if (objectsContentSpin == null)
            return;
        binding.txtContent.setText(objectsContentSpin.getContent());
        binding.ivEdit.setOnClickListener(view -> {
            consumer.accept(new OBJECT_ITEM_CONTENT(TYPE_ACTION.EDIT, position));
        });
        binding.ivDelete.setOnClickListener(view -> {
            consumer.accept(new OBJECT_ITEM_CONTENT(TYPE_ACTION.DELETE, position));
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemContentSpinBinding binding;

        public ViewHolder(@NonNull ItemContentSpinBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public class OBJECT_ITEM_CONTENT {
        TYPE_ACTION type_action;
        int index;

        public OBJECT_ITEM_CONTENT(TYPE_ACTION type_action, int index) {
            this.type_action = type_action;
            this.index = index;
        }

        public TYPE_ACTION getType_action() {
            return type_action;
        }

        public int getIndex() {
            return index;
        }
    }

}
