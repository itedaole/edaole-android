package com.john.groupbuy;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DragListView extends ListView implements OnScrollListener {

	private final static int RATIO = 2;// 手势下拉距离比.

	// 拖拉ListView枚举所有状态
	public enum HeaderState {
		LV_NORMAL, // 普通状态
		LV_PULL_REFRESH, // 下拉状态（为超过mHeadViewHeight）
		LV_RELEASE_REFRESH, // 松开可刷新状态（超过mHeadViewHeight）
		LV_LOADING;// 加载状态
	}

	public enum FooterState {
		NORMAL, LOADING, ERROR, COMPLETED,NO_DATA
	}

	private View mHeadView;// 头部headView
	private Context mContext;
	private int mHeadViewHeight;
	private View mFootView;
	private OnLoadingMoreListener loadingMoreListener = null;
	private HeaderState headerState = HeaderState.LV_NORMAL;
	private FooterState footerState = FooterState.NORMAL;

	private boolean atTop = false;
	private boolean atBottom = false;
	private boolean mIsRecord = false;
	private boolean mBack = false;
	private int mMoveY;
	private int mStartY;

	private ProgressBar mHeadProgressBar;
	private ProgressBar mFooterProgressBar;
	private TextView headerLayout;
	private TextView footerLayout;
	private RotateAnimation animation;
	private RotateAnimation reverseAnimation;
	private ImageView mArrowImageView;

	public DragListView(Context context) {
		super(context);
		mContext = context;
		init();
	}

	/***
	 * 初始化动画
	 */
	private void initAnimation() {
		// 旋转动画
		animation = new RotateAnimation(0, -180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setInterpolator(new LinearInterpolator());// 匀速
		animation.setDuration(250);
		animation.setFillAfter(true);// 停留在最后状态.
		// 反向旋转动画
		reverseAnimation = new RotateAnimation(-180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		reverseAnimation.setInterpolator(new LinearInterpolator());
		reverseAnimation.setDuration(250);
		reverseAnimation.setFillAfter(true);
	}

	public DragListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	public DragListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init();
	}

	public void updateFooterState(FooterState state) {
		switch (state) {
		case NORMAL:
			mFooterProgressBar.setVisibility(View.GONE);
			footerLayout.setText(R.string.state_normal);
			break;
		case LOADING:
			mFooterProgressBar.setVisibility(View.VISIBLE);
			footerLayout.setText(R.string.loading_string);
			break;
		case COMPLETED:
			mFooterProgressBar.setVisibility(View.GONE);
			footerLayout.setText(R.string.state_completed);
			break;
		case ERROR:
			mFooterProgressBar.setVisibility(View.GONE);
			footerLayout.setText(R.string.state_error);
			break;
		case NO_DATA:
			mFooterProgressBar.setVisibility(View.GONE);
			footerLayout.setText(R.string.no_data);
			break;
		}
		footerState = state;
	}

	private void init() {
		initAnimation();
		AddHeadView();
		AddLoadMoreView();
		setOnScrollListener(this);
	}

	public void AddHeadView() {
		mHeadView = LayoutInflater.from(mContext).inflate(R.layout.list_head,
				null);
		mHeadProgressBar = (ProgressBar) mHeadView
				.findViewById(R.id.head_progressBar);
		mArrowImageView = (ImageView) mHeadView
				.findViewById(R.id.head_arrowImageView);

		headerLayout = (TextView) mHeadView
				.findViewById(R.id.head_tipsTextView);
		measureView(mHeadView);
		// 获取宽和高
		mHeadViewHeight = mHeadView.getMeasuredHeight();
		mHeadView.setPadding(0, -1 * mHeadViewHeight, 0, 0); // set padding of
																// header view
		addHeaderView(mHeadView, null, false);
	}

	public void AddLoadMoreView() {
		mFootView = LayoutInflater.from(mContext)
				.inflate(R.layout.footer, null);
		mFooterProgressBar = (ProgressBar) mFootView
				.findViewById(R.id.pb_load_more);
		footerLayout = (TextView) mFootView.findViewById(R.id.tv_loading_more);
		footerLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(loadingMoreListener != null){
					 if(loadingMoreListener.couldLoadMore())					
						 loadingMoreListener.onLoadMore();
					 else
						 updateFooterState(FooterState.COMPLETED);
				}
			}
		});
		addFooterView(mFootView);
	}

	/***
	 * 作用：测量 headView的宽和高.
	 * 
	 * @param child
	 */
	private void measureView(View child) {
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
					MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);
	}

	public void initComplete(boolean isHadLoadMore) {

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		atTop = (firstVisibleItem == 0);
		atBottom = (firstVisibleItem + visibleItemCount == totalItemCount - 1);
		if (atBottom && footerState != FooterState.LOADING
				&& headerState != HeaderState.LV_LOADING
				&& loadingMoreListener != null) {
			if (loadingMoreListener.couldLoadMore()) {
				loadingMoreListener.onLoadMore();
				updateFooterState(FooterState.LOADING);
			} else {
				updateFooterState(FooterState.COMPLETED);
			}
		}
	}

	// 切换headview视图
	public void updateHeaderState(HeaderState state) {
		switch (state) {
		// 普通状态
		case LV_NORMAL: {
			mArrowImageView.clearAnimation();// 清除动画
			mArrowImageView.setImageResource(R.drawable.arrow_re);
		}
			break;
		// 下拉状态
		case LV_PULL_REFRESH: {
			mHeadProgressBar.setVisibility(View.INVISIBLE);// 隐藏进度条
			mArrowImageView.setVisibility(View.VISIBLE);// 下拉图标
			headerLayout.setText("下拉刷新");
			mArrowImageView.clearAnimation();// 清除动画

			// 是有可刷新状态（LV_RELEASE_REFRESH）转为这个状态才执行，其实就是你下拉后在上拉会执行.
			if (mBack) {
				mBack = false;
				mArrowImageView.clearAnimation();// 清除动画
				mArrowImageView.startAnimation(reverseAnimation);// 启动反转动画
			}
		}
			break;
		// 松开刷新状态
		case LV_RELEASE_REFRESH: {
			mHeadProgressBar.setVisibility(View.GONE);// 隐藏进度条
			mArrowImageView.setVisibility(View.VISIBLE);// 显示下拉图标
			headerLayout.setText("释放刷新");
			mArrowImageView.clearAnimation();// 清除动画
			mArrowImageView.startAnimation(animation);// 启动动画
		}
			break;
		// 加载状态
		case LV_LOADING: {
			mHeadProgressBar.setVisibility(View.VISIBLE);
			mArrowImageView.clearAnimation();
			mArrowImageView.setVisibility(View.GONE);
			headerLayout.setText("载入中...");
		}
			break;
		}
		// 切记不要忘记时时更新状态。
		headerState = state;

	}

	/***
	 * 下拉刷新
	 */
	private void onRefresh() {
		if (loadingMoreListener != null) {
			loadingMoreListener.onRefresh();
		}
	}

	/***
	 * 下拉刷新完毕
	 */
	public void onRefreshComplete(boolean over) {
		mHeadView.setPadding(0, -1 * mHeadViewHeight, 0, 0);// 回归.
		updateHeaderState(HeaderState.LV_NORMAL);// if
	}

	/***
	 * 自定义接口
	 */
	public interface OnLoadingMoreListener {
		/***
		 * // 下拉刷新执行
		 */
		void onRefresh();

		/**
		 * could load more data
		 * 
		 * @return whether could load more data
		 */
		boolean couldLoadMore();

		/***
		 * 点击加载更多
		 */
		void onLoadMore();
	}

	/***
	 * touch 事件监听
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		// 按下
		case MotionEvent.ACTION_DOWN:
			// record current position y
			doActionDown(ev);
			break;
		// 移动
		case MotionEvent.ACTION_MOVE:
			doActionMove(ev);
			break;
		// 抬起
		case MotionEvent.ACTION_UP:
			doActionUp(ev);
			break;
		default:
			break;
		}
		/***
		 * 如果是ListView本身的拉动，那么返回true，这样ListView不可以拖动.
		 * 如果不是ListView的拉动，那么调用父类方法，这样就可以上拉执行.
		 */

		return super.onTouchEvent(ev);

	}

	/***
	 * 摁下操作
	 * 
	 * 作用：获取摁下是的y坐标
	 * 
	 * @param event
	 */
	void doActionDown(MotionEvent event) {
		if (mIsRecord == false && atTop) {
			mStartY = (int) event.getY();
			mIsRecord = true;
		}
	}

	/***
	 * 拖拽移动操作
	 * 
	 * @param event
	 */
	void doActionMove(MotionEvent event) {
		mMoveY = (int) event.getY();// 获取实时滑动y坐标
		// 检测是否是一次touch事件.
		if (mIsRecord == false && atTop && footerState != FooterState.LOADING) {
			mStartY = (int) event.getY();
			mIsRecord = true;
		}
		/***
		 * 如果touch关闭或者正处于Loading状态的话 return.
		 */
		if (mIsRecord == false || headerState == HeaderState.LV_LOADING) {
			return;
		}
		// 向下啦headview移动距离为y移动的一半.（比较友好）
		int offset = (mMoveY - mStartY) / RATIO;
		
		if(offset > 0 && footerState == FooterState.LOADING)
			return;

		if(offset <0 && headerState == HeaderState.LV_LOADING)
			return;
		
		switch (headerState) {
		// 普通状态
		case LV_NORMAL: {
			// 如果<0，则意味着上滑动.
			if (offset > 0) { // 向下滑动
				// 设置headView的padding属性.
				mHeadView.setPadding(0, offset - mHeadViewHeight, 0, 0);
				updateHeaderState(HeaderState.LV_PULL_REFRESH);// 下拉状态
			}

		}
			break;
		// 下拉状态
		case LV_PULL_REFRESH: {
			setSelection(0);// 选中第一项，可选.
			// 设置headView的padding属性.
			mHeadView.setPadding(0, offset - mHeadViewHeight, 0, 0);
			if (offset < 0) {
				updateHeaderState(HeaderState.LV_NORMAL);// 普通状态
			} else if (offset > mHeadViewHeight) {// 如果下拉的offset超过headView的高度则要执行刷新.
				updateHeaderState(HeaderState.LV_RELEASE_REFRESH);// 更新为可刷新的下拉状态.
			}
		}
			break;
		// 可刷新状态
		case LV_RELEASE_REFRESH: {
			setSelection(0);
			// 设置headView的padding属性.
			mHeadView.setPadding(0, offset - mHeadViewHeight, 0, 0);
			// 下拉offset>0，但是没有超过headView的高度.那么要goback 原装.
			if (offset >= 0 && offset <= mHeadViewHeight) {
				mBack = true;
				updateHeaderState(HeaderState.LV_PULL_REFRESH);
			} else if (offset < 0) {
				updateHeaderState(HeaderState.LV_NORMAL);
			} else {

			}
		}
			break;
		default:
			return;
		}
		;
	}

	/***
	 * 手势抬起操作
	 * 
	 * @param event
	 */
	public void doActionUp(MotionEvent event) {
		mIsRecord = false;// 此时的touch事件完毕，要关闭。
		mBack = false;
		// 如果下拉状态处于loading状态.
		if (headerState == HeaderState.LV_LOADING) {
			return;
		}
		// 处理相应状态.
		switch (headerState) {
		// 普通状态
		case LV_NORMAL:

			break;
		// 下拉状态
		case LV_PULL_REFRESH:
			mHeadView.setPadding(0, -1 * mHeadViewHeight, 0, 0);
			updateHeaderState(HeaderState.LV_NORMAL);
			break;
		// 刷新状态
		case LV_RELEASE_REFRESH:
			mHeadView.setPadding(0, 0, 0, 0);
			updateHeaderState(HeaderState.LV_LOADING);
			onRefresh();// 下拉刷新
			break;
		default:
			break;
		}

	}

	// 注入下拉刷新接口
	public void setOnRefreshListener(
			OnLoadingMoreListener onRefreshLoadingMoreListener) {
		this.loadingMoreListener = onRefreshLoadingMoreListener;
	}

	public void removeHeadView() {
		super.removeHeaderView(mHeadView);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

}
