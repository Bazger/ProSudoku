package com.example.ProSudoku.activity.scores;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import com.example.ProSudoku.DB;
import com.example.ProSudoku.R;

public class ScoresFragment extends Fragment {

	ListView lvData;
	DB db;
	SimpleCursorAdapter scAdapter;
	int pageNum;

	private static final String TAG = "Scores fragment";

	static ScoresFragment newInstance(int page) {
		ScoresFragment pageFragment = new ScoresFragment();
		Bundle arguments = new Bundle();
		arguments.putInt(TAG, page);
		pageFragment.setArguments(arguments);
		return pageFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.scores_fragment, container, false);

		// открываем подключение к БД
		db = new DB(getActivity());
		db.open();
		pageNum = getArguments().getInt(TAG);

		Cursor c =  db.getQuery(null, DB.COLUMN_DIFFICULTY + " == ?", new String[]{getArguments().getInt(TAG) + ""}, null, null, DB.COLUMN_TIME);

		String[] str = new String[c.getCount()];
		for(int i = 0; i < str.length; i++){
			str[i] =  i + 1 + "";
		}

		// формируем столбцы сопоставления
		String[] from = new String[]{DB._ID, DB.COLUMN_NAME, DB.COLUMN_TIME};
		int[] to = new int[]{R.id.tvID, R.id.tvName, R.id.tvTime};

		// создааем адаптер и настраиваем список
		scAdapter = new SimpleCursorAdapter(getActivity(),
				R.layout.scores_item,c, from, to, 0);

		if(db.getAllData().getCount() == 0)
		{
			TextView tv = (TextView) rootView.findViewById(R.id.scoresText);
			tv.setVisibility(View.VISIBLE);
		}
		scAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			@Override
			public boolean setViewValue(final View view, final Cursor cursor, final int columnIndex) {
				if (columnIndex == 0) {
					TextView textView = (TextView) view;
					int CursorPos = cursor.getPosition() + 1;
					textView.setText(Integer.toString(CursorPos) + ".");
					return true;
				}
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


		/*Button add_button = (Button) rootView.findViewById(R.id.add_button);
		// if button is clicked, close the custom dialog
		add_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				db.open();
				db.addRec("sometext " + getArguments().getInt(TAG), 10, DB.Dif.values()[getArguments().getInt(TAG)]);
				scAdapter.notifyDataSetChanged();
			}
		});*/

		return rootView;
	}
}

