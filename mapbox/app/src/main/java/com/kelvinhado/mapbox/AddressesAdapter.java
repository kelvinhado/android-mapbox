package com.kelvinhado.mapbox;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.kelvinhado.mapbox.model.Address;

import java.util.List;

/**
 * Created by kelvin on 31/07/2017.
 */

public class AddressesAdapter extends ArrayAdapter {

    List<Address> addressList;
    LayoutInflater mInflater;
    Context context;

    public AddressesAdapter(Context context, List<Address> list) {
        super(context, 0, list);
        this.addressList = list;
        this.context = context;
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.drawer_list_item, parent, false);
            holder = new ViewHolder();
            holder.address = (TextView) convertView.findViewById(R.id.tv_address);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
        }

        Address address = addressList.get(position);
        holder.address.setText(address.getPlaceName());
        return convertView;

    }

    static class ViewHolder {
        TextView address;
    }
}
