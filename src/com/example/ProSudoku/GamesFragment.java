package com.example.ProSudoku;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class GamesFragment extends Fragment {

	ListView lvData;
	DB db;
	SimpleCursorAdapter scAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_games, container, false);

		// открываем подключение к БД
		db = new DB(getActivity());
		db.open();

		// формируем столбцы сопоставления
		String[] from = new String[] { DB.COLUMN_NAME, DB.COLUMN_TIME };
		int[] to = new int[] { R.id.tvName, R.id.tvTime};

		ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.pager);
		Log.d("MY", viewPager.getCurrentItem() + "");
		Cursor c = db.getQuery(null, DB.COLUMN_DIFFICULTY + " == ?", new String[]{viewPager.getCurrentItem() + ""}, null, null, null);

		// создааем адаптер и настраиваем список
		scAdapter = new SimpleCursorAdapter(getActivity(), R.layout.records_item, c, from, to, 0);

		scAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			@Override
			public boolean setViewValue(final View view, final Cursor cursor, final int columnIndex) {
				if (columnIndex == 2) {
					TextView textView = (TextView) view;
					long seconds = cursor.getLong(columnIndex);
					textView.setText(String.format("%02d:%02d", seconds / 60, seconds % 60));
					return true;
				}

				return false;
			}

		});

		lvData = (ListView) rootView.findViewById(R.id.lvData);
		lvData.setAdapter(scAdapter);


		Button add_button = (Button) rootView.findViewById(R.id.add_button);
		// if button is clicked, close the custom dialog
		add_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});

		return rootView;
	}
}

