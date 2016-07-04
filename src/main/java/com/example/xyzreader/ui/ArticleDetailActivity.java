package com.example.xyzreader.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.LoginFilter;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xyzreader.Article;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.data.UpdaterService;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String ARTICLE_DETAILS = "article_details";

    private Cursor mCursor;
    private long mStartId;
    private Toolbar mToolbar;

    private long mSelectedItemId;
    private int mSelectedItemUpButtonFloor = Integer.MAX_VALUE;
    private int mTopInset;
    private boolean mIsReturning;
    private int mCurrentPosition;
    private int mStartingPosition;

    private String photoURL;

    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;
    private Activity mContext;
    private ImageView mPhotoView, mAlbumImage;
    private TextView mTextViewTitle, mTextViewAuthor, mTextViewBody;
    private View mUpButtonContainer;
    private View mUpButton;
    private Article temp;

    private static final String TAG = "ArticleDetailActivity";
    private static final String STATE_CURRENT_PAGE_POSITION = "state_current_page_position";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_article_detail);
        Log.i(TAG, "onCreate: ");
        supportPostponeEnterTransition();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        mStartingPosition = getIntent().getIntExtra(ArticleListActivity.EXTRA_STARTING_ALBUM_POSITION, 0);
        if (savedInstanceState == null) {
            mCurrentPosition = mStartingPosition;
        } else {
            Log.i(TAG,"-----------------"+savedInstanceState.isEmpty());
            mCurrentPosition = savedInstanceState.getInt(STATE_CURRENT_PAGE_POSITION);
        }

        mContext = this;

        getLoaderManager().initLoader(0, null, this);

        mPhotoView = (ImageView) findViewById(R.id.thumbnail);
        mTextViewTitle = (TextView) findViewById(R.id.detail_title);
        mTextViewAuthor = (TextView) findViewById(R.id.detail_author);
        mTextViewBody = (TextView) findViewById(R.id.detail_body);

        findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(mContext)
                        .setType("text/plain")
                        .setText(mTextViewBody.getText())
                        .getIntent(), getString(R.string.action_share)));
            }
        });

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mStartId = ItemsContract.Items.getItemId(getIntent().getData());
                mSelectedItemId = mStartId;
            }
        } else {

            Article temp = savedInstanceState.getParcelable(ARTICLE_DETAILS);
            Picasso.with(this).load(temp.getPhotoURL()).placeholder(R.mipmap.ic_launcher).error(R.mipmap.symbols_warning).fit().into( mPhotoView);
            mTextViewTitle.setText(temp.getTitle());
            mTextViewAuthor.setText(temp.getAuthor());
            mTextViewBody.setText(temp.getBody());
            photoURL = temp.getPhotoURL();
        }


        getWindow().getEnterTransition().setDuration(500);

        final View layout = findViewById(R.id.main_container);
        ViewTreeObserver vto = layout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                startPostponedEnterTransition();
            }
        });


        // stackoverflow solution http://stackoverflow.com/questions/31662416/show-collapsingtoolbarlayout-title-only-when-collapsed
        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
        final ImageView toolbarImage = (ImageView) findViewById(R.id.image_toolbar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle(mTextViewTitle.getText());
                    isShow = true;
                } else if(isShow) {
                    collapsingToolbarLayout.setTitle("");
                    isShow = false;
                }
            }
        });
        // stackoverflow end
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop: ");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.i(TAG, "onCreateLoader: ");
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;
        //mPagerAdapter.notifyDataSetChanged();

        // Select the start ID
        if (mStartId > 0) {
            mCursor.moveToFirst();
            // TODO: optimize
            while (!mCursor.isAfterLast()) {
                if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
                    final int position = mCursor.getPosition();
                    Log.i(TAG, "onLoadFinished: "+position);
                    Log.i(TAG, "onLoadFinished: "+mCursor.getString(ArticleLoader.Query.TITLE));
                    photoURL = mCursor.getString(ArticleLoader.Query.PHOTO_URL);
                    Picasso.with(this).load(photoURL).placeholder(R.mipmap.ic_launcher).error(R.mipmap.symbols_warning).fit().into( mPhotoView);
                    mTextViewTitle.setText(mCursor.getString(ArticleLoader.Query.TITLE));
                    mTextViewAuthor.setText(Html.fromHtml(
                            DateUtils.getRelativeTimeSpanString(
                                    mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                                    System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                    DateUtils.FORMAT_ABBREV_ALL).toString()
                                    + " by <font color='#ffffff'>"
                                    + mCursor.getString(ArticleLoader.Query.AUTHOR)
                                    + "</font>"));
                    mTextViewBody.setText(mCursor.getString(ArticleLoader.Query.BODY));
                    break;
                }
                mCursor.moveToNext();
            }
            mStartId = 0;
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
    }


    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            ArticleDetailFragment fragment = (ArticleDetailFragment) object;
            if (fragment != null) {
//                mSelectedItemUpButtonFloor = fragment.getUpButtonFloor();
//                updateUpButtonPosition();
            }
        }

        @Override
        public Fragment getItem(int position) {
            mCursor.moveToPosition(position);
            return ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID));
        }

        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(TAG, "onSaveInstanceState: *********************");
        Log.i(TAG, "onSaveInstanceState: 1"+photoURL);
        Log.i(TAG, "onSaveInstanceState: 2"+mTextViewTitle.getText().toString());
        Log.i(TAG, "onSaveInstanceState: 3"+mTextViewAuthor.getText().toString());
        Log.i(TAG, "onSaveInstanceState: 4"+mTextViewBody.getText().toString());
        if(temp == null){
            temp = new Article(photoURL,mTextViewTitle.getText().toString(),mTextViewAuthor.getText().toString(),mTextViewBody.getText().toString());
        } else {
            Log.i(TAG, "onSaveInstanceState: 5"+temp.toString());
        }
        outState.putParcelable(ARTICLE_DETAILS, temp);
    }

    private void getDataFromCursor(Cursor cursor){



    }

}
