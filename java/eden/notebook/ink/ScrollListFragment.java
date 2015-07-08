package eden.notebook.ink;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class ScrollListFragment extends Fragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedinstancestate){
        LinearLayout viewgroup = (LinearLayout) inflater.inflate(R.layout.layout_fragment_scroll, container, false);
        RecyclerView mRecyclerView = (RecyclerView) viewgroup.findViewById(R.id.view_recycler_scroll);
        mRecyclerView.setAdapter(new ScrollAdapter(container.getContext()));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        return viewgroup;
    }
}