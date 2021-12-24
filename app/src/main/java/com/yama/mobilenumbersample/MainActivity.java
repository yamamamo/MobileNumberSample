package com.yama.mobilenumbersample;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements TextWatcher {
    private String TAG = "MainActivity";
    private Context mContext;
    private ArrayList<Data> mArrayList, mFilteredList;//필터링할 데이터 담을 어레이리스트
    private Adapter mAdapter;
    private RecyclerView mRecyclerView;
    private EditText edit_name, edit_number, edit_search;
    private Button btn_save;
    private DBHelper mDbHelper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);
        mContext = MainActivity.this;
        edit_name = findViewById (R.id.edit_name);
        edit_number = findViewById (R.id.edit_number);
        btn_save = findViewById (R.id.btn_save);
        mRecyclerView = findViewById (R.id.recycler);
        edit_search = findViewById (R.id.edit_search);
        edit_search.addTextChangedListener (this);

        //DBHelper 객체를 선언해줍니다.
        mDbHelper = new DBHelper (mContext);
        //쓰기모드에서 데이터 저장소를 불러옵니다.
        db = mDbHelper.getWritableDatabase ();
        
        initRecyclerView ();

        //버튼 클릭이벤트
        //이름과 전화번호를 입력한 후 버튼을 클릭하면 어레이리스트에 데이터를 담고 리사이클러뷰에 띄운다.
        btn_save.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                if (edit_name.getText ().length () == 0 && edit_number.getText ().length () == 0) {
                    Toast.makeText (mContext, "이름과 전화번호를 입력해주세요", Toast.LENGTH_SHORT).show ();
                } else {
                    String name = edit_name.getText ().toString ();
                    String number = edit_number.getText ().toString ();
                    edit_name.setText ("");
                    edit_number.setText ("");
                    Data data = new Data (name, number);

                    mArrayList.add (data);
                    mAdapter.notifyItemInserted (mArrayList.size () - 1);

                    //데이터를 테이블에 삽입합니다.
                    insertNumber (name, number);

                }
            }
        });

        //리사이클러뷰 클릭 이벤트
        mAdapter.setOnItemClickListener (new Adapter.OnItemClickListener () {

            //아이템 클릭시 토스트메시지0
            @Override
            public void onItemClick(View v, int position) {
                String name = mArrayList.get (position).getName ();
                String number = mArrayList.get (position).getNumber ();
                //Toast.makeText (mContext, "이름 : " + name + "\n전화번호 : " + number, Toast.LENGTH_SHORT).show ();

                //인텐트 객체 생성.
                //Intent 에 putExtra 로  name, number 담는다.
                //startActivity 를 사용해 DetailActivity 를 호출한다.
                Intent intent = new Intent (mContext, DetailActivity.class);
                intent.putExtra ("name", name);
                intent.putExtra ("number", number);
                startActivity (intent);
            }

            //수정
            @Override
            public void onEditClick(View v, int position) {
                String name = mArrayList.get (position).getName ();
                String number = mArrayList.get (position).getNumber ();

                editItem (name, number, position);
            }

            //삭제
            @Override
            public void onDeleteClick(View v, int position) {
                String name = mArrayList.get (position).getName ();
                String number = mArrayList.get (position).getNumber ();

                //
                deleteNumber (name, number);

                mArrayList.remove (position);
                mAdapter.notifyItemRemoved (position);
            }

        });

    }

    //SQLite 데이터 수정
    //newName 은 수정된 값, oldName 수정전 값
    private void updateNumber(String oldName, String oldNumber, String newName, String newNumber){
        //수정된 값들을 values 에 추가한다.
        ContentValues values = new ContentValues();
        values.put(DBHelper.FeedEntry.COLUMN_NAME_NAME, newName);
        values.put (DBHelper.FeedEntry.COLUMN_NAME_NUMBER, newNumber);

        // WHERE 절 수정될 열을 찾는다.
        String selection = DBHelper.FeedEntry.COLUMN_NAME_NAME + " LIKE ?" +
                        " AND "+ DBHelper.FeedEntry.COLUMN_NAME_NUMBER + " LIKE ?";
        String[] selectionArgs = { oldName, oldNumber };

        db.update(DBHelper.FeedEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    //SQLite 데이터 삭제
    private void deleteNumber(String name, String number) {
        //WHERE 절 삭제될 열을 찾는다.
        String selection = DBHelper.FeedEntry.COLUMN_NAME_NAME + " LIKE ?" +
                " and " + DBHelper.FeedEntry.COLUMN_NAME_NUMBER + " LIKE ?";
        String[] selectionArgs = {name, number};
        db.delete (DBHelper.FeedEntry.TABLE_NAME, selection, selectionArgs);
    }

    //SQLite 데이터 삽입
    private void insertNumber(String name, String number) {
        //쿼리를 직접 작성해서 입력하거나 values를 만들어서 하는 방법이 있다
        //후자를 이용하겠다.
        ContentValues values = new ContentValues ();
        values.put (DBHelper.FeedEntry.COLUMN_NAME_NAME, name);
        values.put (DBHelper.FeedEntry.COLUMN_NAME_NUMBER, number);
        db.insert (DBHelper.FeedEntry.TABLE_NAME, null, values);
//        String sql = "INSERT INTO "+DBHelper.FeedEntry.TABLE_NAME+" values("+name+", "+number+");";
//        db.execSQL(sql);


    }

    //데이터 불러오기
    //Cursor를 사용해서 데이터를 불러옵니다.
    //while문을 사용해서 불러온 데이터를 mArrayList에 삽입합니다.
    private void loadData() {

        @SuppressLint("Recycle") Cursor c = db.rawQuery ("SELECT * FROM " + DBHelper.FeedEntry.TABLE_NAME, null);
        while (c.moveToNext ()) {
//            Log.d (TAG, c.getString (c.getColumnIndex (DBHelper.FeedEntry._ID))
//                    + " name-"+c.getString(c.getColumnIndex(DBHelper.FeedEntry.COLUMN_NAME_NAME))
//                    + " number-"+c.getString(c.getColumnIndex(DBHelper.FeedEntry.COLUMN_NAME_NUMBER)));
            String name = c.getString (c.getColumnIndex (DBHelper.FeedEntry.COLUMN_NAME_NAME));
            String number = c.getString (c.getColumnIndex (DBHelper.FeedEntry.COLUMN_NAME_NUMBER));
            Data data = new Data (name, number);

            mArrayList.add (data);
        }

        mAdapter.notifyDataSetChanged ();
    }

    //리사이클러뷰
    private void initRecyclerView() {
        //레이아웃메니저는 리사이클러뷰의 항목 배치를 어떻게 할지 정하고, 스크롤 동작도 정의한다.
        //수평/수직 리스트 LinearLayoutManager
        //그리드 리스트 GridLayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager (mContext, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager (layoutManager);
        mFilteredList = new ArrayList<> ();
        mArrayList = new ArrayList<> ();
        mAdapter = new Adapter (mContext, mArrayList);
        mRecyclerView.setAdapter (mAdapter);

        //저장된 데이터를 불러옵니다.
        loadData ();

    }

    //AlertDialog 를 사용해서 데이터를 수정한다.
    private void editItem(String name, String number, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder (this);
        View view = LayoutInflater.from (this).inflate (R.layout.dialog, null, false);
        builder.setView (view);

        final AlertDialog dialog = builder.create ();

        final Button btn_edit = view.findViewById (R.id.btn_edit);
        final Button btn_cancel = view.findViewById (R.id.btn_cancel);
        final EditText edit_name = view.findViewById (R.id.edit_editName);
        final EditText edit_number = view.findViewById (R.id.edit_editNumber);

        edit_name.setText (name);
        edit_number.setText (number);


        // 수정 버튼 클릭
        //어레이리스트 값을 변경한다.
        btn_edit.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                String editName = edit_name.getText ().toString ();
                String editNumber = edit_number.getText ().toString ();
                mArrayList.get (position).setName (editName);
                mArrayList.get (position).setNumber (editNumber);

                //데이터 수정
                updateNumber (name,number,editName,editNumber);

                mAdapter.notifyItemChanged (position);
                dialog.dismiss ();
            }
        });

        // 취소 버튼 클릭
        btn_cancel.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                dialog.dismiss ();
            }
        });

        dialog.show ();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy ();
        mDbHelper.close ();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//        mAdapter.getFilter ().filter (charSequence);
    }

    //에딧텍스트에 입력받는 값을 감지한다.
    @Override
    public void afterTextChanged(Editable editable) {
        String searchText = edit_search.getText().toString();
        searchFilter(searchText);
    }

    //에딧텍스트 값을 받아 mFilteredList에 데이터를 추가한다.
    public void searchFilter(String searchText) {
        mFilteredList.clear();

        for (int i = 0; i < mArrayList.size(); i++) {
            if (mArrayList.get(i).getName().toLowerCase().contains(searchText.toLowerCase())) {
                mFilteredList.add(mArrayList.get(i));
            }
        }
        mAdapter.listFilter (mFilteredList);
    }

}
