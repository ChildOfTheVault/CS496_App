package casco.project1.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.afollestad.dragselectrecyclerview.DragSelectRecyclerViewAdapter;

import casco.project1.R;
import casco.project1.dataBackend.RectangleView;

/**
 * Created by christianmello on 5/3/16.
 */
public class PollCreationAdapter extends DragSelectRecyclerViewAdapter<PollCreationAdapter.MainViewHolder> {

    private final static String[] ALPHABET = "8:00 8:30 9:00 9:30 10:00 10:30 11:00 11:30 12:00 12:30 1:00 1:30 2:00 2:30 3:00 3:30 4:00 4:30 5:00 5:30 6:00 6:30 7:00 7:30 8:00 8:30".split(" ");
    private final static int[] COLORS = new int[]{
            Color.parseColor("#F44336"),
            Color.parseColor("#E91E63"),
            Color.parseColor("#9C27B0"),
            Color.parseColor("#673AB7"),
            Color.parseColor("#3F51B5"),
            Color.parseColor("#2196F3"),
            Color.parseColor("#03A9F4"),
            Color.parseColor("#00BCD4"),
            Color.parseColor("#009688"),
            Color.parseColor("#4CAF50"),
            Color.parseColor("#8BC34A"),
            Color.parseColor("#CDDC39"),
            Color.parseColor("#FFEB3B"),
            Color.parseColor("#FFC107"),
            Color.parseColor("#FF9800"),
            Color.parseColor("#FF5722"),
            Color.parseColor("#795548"),
            Color.parseColor("#9E9E9E"),
            Color.parseColor("#607D8B"),
            Color.parseColor("#F44336"),
            Color.parseColor("#E91E63"),
            Color.parseColor("#9C27B0"),
            Color.parseColor("#673AB7"),
            Color.parseColor("#3F51B5"),
            Color.parseColor("#2196F3"),
            Color.parseColor("#03A9F4")
    };

    public interface ClickListener {
        void onClick(int index);

        void onLongClick(int index);
    }

    private final ClickListener mCallback;

    protected PollCreationAdapter(ClickListener callback) {
        super();
        mCallback = callback;
    }

    public String getItem(int index) {
        return ALPHABET[index];
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.griditem_poll_creation, parent, false);
        return new MainViewHolder(v, mCallback);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        holder.label.setText(getItem(position));

        final Drawable d;
        final Context c = holder.itemView.getContext();

        if (isIndexSelected(position)) {
            d = new ColorDrawable(ContextCompat.getColor(c, R.color.grid_foreground_selected));
            holder.label.setTextColor(ContextCompat.getColor(c, R.color.grid_label_text_selected));
        } else {
            d = null;
            holder.label.setTextColor(ContextCompat.getColor(c, R.color.grid_label_text_normal));
        }

        //noinspection RedundantCast
        ((FrameLayout) holder.colorSquare).setForeground(d);
        holder.colorSquare.setBackgroundColor(COLORS[position]);
    }

    @Override
    public int getItemCount() {
        return ALPHABET.length;
    }

    public static class MainViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        public final TextView label;
        public final RectangleView colorSquare;
        private final ClickListener mCallback;

        public MainViewHolder(View itemView, ClickListener callback) {
            super(itemView);
            mCallback = callback;

            this.label = (TextView) itemView.findViewById(R.id.label);
            this.colorSquare = (RectangleView) itemView.findViewById(R.id.colorSquare);
            this.itemView.setOnClickListener(this);
            this.itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mCallback != null)
                mCallback.onClick(getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            if (mCallback != null)
                mCallback.onLongClick(getAdapterPosition());
            return true;
        }
    }
}
