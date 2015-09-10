package com.ywxy.laowang.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ywxy.laowang.R;
import com.ywxy.laowang.common.bean.LaowangItemList;

/**
 * Created by hjw on 2015/8/29 0029.
 */
public class LaowangListAdapter extends RecyclerView.Adapter<LaowangListAdapter.LaowangViewHolder> {

    private View loadMoreView = null;
    private View endFooterView = null;


    private LaowangItemList mLaowangList = new LaowangItemList();
    private Context context;

    private OnItemClickListener listener;
    private boolean isNeedLoadMore = false;

    public LaowangListAdapter(Context context) {
        this.context = context;
    }

    public LaowangItemList getData() {
        return mLaowangList;
    }

    public void setData(LaowangItemList mLaowangList) {
        this.mLaowangList.mLaowangList.clear();
        this.mLaowangList.mLaowangList.addAll(mLaowangList.mLaowangList);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void appendData(LaowangItemList mLaowangList) {
        this.mLaowangList.mLaowangList.addAll(mLaowangList.mLaowangList);
        notifyDataSetChanged();
    }

    public void setFooterView(View loadMoreView, View endFooterView) {
        this.loadMoreView = loadMoreView;
        this.endFooterView = endFooterView;
    }

    public void setIsNeedLoadMore(boolean isLoadMore) {
        this.isNeedLoadMore = isLoadMore;
    }

    public boolean isNeedLoadMore() {
        return isNeedLoadMore;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1 && isNeedLoadMore())
            return VIEW_TYPES.TYPE_LOADMORE_FOOTER;
        else if (position == getItemCount() - 1 && !isNeedLoadMore())
            return VIEW_TYPES.TYPE_LOADEND_FOOTER;
        else
            return VIEW_TYPES.TYPE_NORMAL;
    }

    @Override
    public int getItemCount() {
        return getAdapterItemCount() + (hasFooterView() ? 1 : 0);
    }

    @Override
    public LaowangViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPES.TYPE_LOADMORE_FOOTER) {
            return new LaowangViewHolder(loadMoreView, false);
        } else if (viewType == VIEW_TYPES.TYPE_LOADEND_FOOTER) {
            return new LaowangViewHolder(endFooterView, false);
        } else {
            return new LaowangViewHolder(LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.item_list_laowang, parent, false), true);
        }
    }

    @Override
    public void onBindViewHolder(final LaowangViewHolder holder, final int position) {
        if (holder.isItem) {
            String url = mLaowangList.mLaowangList.get(position).item_url;
            Glide.with(context).load(url)
                    .placeholder(R.drawable.ic_default_img)
                    .error(R.drawable.ic_default_img)
                    .crossFade().into(holder.l_img);
            holder.l_text.setText(mLaowangList.mLaowangList.get(position).item_text);
            holder.mRootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onItemClick(position);
                }
            });
        }
    }

    public int getAdapterItemCount() {
        return mLaowangList.mLaowangList.size();
    }

    public boolean hasFooterView() {
        return loadMoreView != null || endFooterView != null;
    }

    public interface OnItemClickListener {
        void onItemClick(int pos);
    }

    public class VIEW_TYPES {
        public static final int TYPE_LOADMORE_FOOTER = 0x101;
        public static final int TYPE_NORMAL = 0x102;
        public static final int TYPE_LOADEND_FOOTER = 0x103;
    }

    public class LaowangViewHolder extends RecyclerView.ViewHolder {

        ImageView l_img;
        ImageView l_share_marker;
        TextView l_text;
        LinearLayout mRootView;
        boolean isItem;

        public LaowangViewHolder(View itemView, boolean isItem) {
            super(itemView);
            if (isItem) {
                this.isItem = true;
                mRootView = (LinearLayout) itemView.findViewById(R.id.id_laowang_img_container);
                l_img = (ImageView) itemView.findViewById(R.id.id_laowang_img);
                l_share_marker = (ImageView) itemView.findViewById(R.id.id_laowang_img_gif_marker);
                l_text = (TextView) itemView.findViewById(R.id.id_laowang_text);

            } else
                this.isItem = false;
        }

    }
}
