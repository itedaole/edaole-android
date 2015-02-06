package com.john.groupbuy;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import com.john.groupbuy.lib.FactoryCenter;
import com.john.groupbuy.lib.http.ConsumeCouponInfo;
import com.john.groupbuy.lib.http.GlobalKey;
import com.john.groupbuy.lib.http.ProductInfo;
import com.john.groupbuy.lib.http.StatusInfo;
import com.john.groupbuy.zxing.CaptureActivity;
import com.john.util.HttpResponseException;
import com.john.util.LogUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;

public class VerifyCouponActivity extends BaseActivity implements
		OnClickListener {
	private String mCouponArray[];
	private ProgressDialog myDialog;

	private EditText mCodeEdt;
	private EditText mSecretEdt;

	private Button mSubmitInputBtn;
	private Button mScanCodeBtn;

	protected GetStatusInfoTask mTask;

	protected class GetStatusInfoTask extends
			AsyncTask<String, Void, StatusInfo> {

		GetStatusInfoTask() {
			super();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			myDialog = new ProgressDialog(VerifyCouponActivity.this);
			myDialog.setOnCancelListener(new OnCancelListener() {

				@Override
				public void onCancel(DialogInterface dialog) {
					mTask.cancel(true);
				}
			});
			myDialog.setIndeterminate(true);
			myDialog.setMessage(getString(R.string.verify_code));
			myDialog.setCancelable(true);
			myDialog.show();

		}

		@Override
		protected StatusInfo doInBackground(String... params) {
			StatusInfo statusInfo = null;
			try {
				statusInfo = FactoryCenter.getProcessCenter().verifyCode(
						params[0]);
			} catch (HttpResponseException e) {
				LogUtil.warn(e.getMessage(), e);
			} catch (IOException e) {
				LogUtil.warn(e.getMessage(), e);
			}
			return statusInfo;
		}

		@Override
		protected void onPostExecute(StatusInfo result) {
			myDialog.dismiss();

			if (result == null || result.getStatus() == null) {
				showToast(R.string.get_coupon_state_error_hint);
				return;
			}

			if (result.getStatus().equalsIgnoreCase("1")) {

				if (result.getProductInfo() != null) {
					View view = getLayoutInflater().inflate(
							R.layout.verify_coupon_dialog, null);
					DialogHolder holder = new DialogHolder(view);
					holder.setData(result.getProductInfo());

					final Dialog dialog = new Dialog(VerifyCouponActivity.this);
					ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
							0, 0);
					DisplayMetrics dm = getResources().getDisplayMetrics();
					int width = (int) (dm.widthPixels - 20 * dm.density);
					params.width = width;
					params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
					dialog.setCancelable(true);
					dialog.setContentView(view,params);
					dialog.setTitle(R.string.prompt);

					holder.setAcceptListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							dialog.dismiss();
							new GetConsumeCouponInfo().execute(mCouponArray[0],
									mCouponArray[1]);
							clearUserInput();
						}
					});

					holder.setCancelListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							dialog.dismiss();
						}
					});

					dialog.show();
				} else {
					new AlertDialog.Builder(VerifyCouponActivity.this)
							.setTitle(R.string.prompt)
							.setMessage(R.string.consume_coupon_hint)
							.setPositiveButton(R.string.accept,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											new GetConsumeCouponInfo().execute(
													mCouponArray[0],
													mCouponArray[1]);
											clearUserInput();
										}
									}).setNegativeButton(R.string.cancel, null)
							.create().show();
				}

			} else {
				// 验证失败
				String message = null;
				if (result != null) {
					message = result.getResult();
				}

				if (TextUtils.isEmpty(message))
					message = getString(R.string.consume_failure);

				new AlertDialog.Builder(VerifyCouponActivity.this)
						.setCancelable(false)
						.setMessage(Html.fromHtml(message))
						.setNeutralButton(getString(R.string.Ensure),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.cancel();

									}

								}).show();
			}
		}

	}

	protected class GetConsumeCouponInfo extends
			AsyncTask<String, Void, ConsumeCouponInfo> {

		@Override
		protected ConsumeCouponInfo doInBackground(String... params) {
			ConsumeCouponInfo res = null;
			String code = params[0];
			String secret = params[1];
			try {
				res = FactoryCenter.getProcessCenter().consumeCoupon(code,
						secret);
			} catch (HttpResponseException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			return res;
		}

		@Override
		protected void onPostExecute(ConsumeCouponInfo result) {
			super.onPostExecute(result);

			if (result == null) {
				Toast.makeText(VerifyCouponActivity.this, R.string.connecting_error,
						Toast.LENGTH_LONG).show();
				return;
			}

			// Toast.makeText(getApplicationContext(),result.data.data.html,Toast.LENGTH_SHORT).show();
			new AlertDialog.Builder(VerifyCouponActivity.this)
					.setCancelable(false)
					.setMessage(Html.fromHtml(result.data.data.html))
					// .setMessage(getString(R.string.consume_coupon_success))
					.setNeutralButton(getString(R.string.Ensure),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
								}
							}).show();
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.verify_coupon);
		enableBackBehavior();
		setTitle(R.string.verify_ticket);
		initViewComponents();
	}

	private void clearUserInput() {
		mCodeEdt.setText("");
		mSecretEdt.setText("");
	}

	protected void initViewComponents() {
		mCodeEdt = (EditText) findViewById(R.id.codeEdit);
		mSecretEdt = (EditText) findViewById(R.id.secretEdit);
		mSubmitInputBtn = (Button) findViewById(R.id.submitInputBtn);
		mScanCodeBtn = (Button) findViewById(R.id.scanCode);
		mSubmitInputBtn.setOnClickListener(this);
		mScanCodeBtn.setOnClickListener(this);

	}

	protected boolean verifyUserInput() {
		if (TextUtils.isEmpty(mCodeEdt.getText().toString())) {
			Toast.makeText(VerifyCouponActivity.this,
					getString(R.string.empty_code), Toast.LENGTH_SHORT).show();
			return false;
		}
		if (TextUtils.isEmpty(mSecretEdt.getText().toString())) {
			Toast.makeText(VerifyCouponActivity.this,
					getString(R.string.empty_secret), Toast.LENGTH_SHORT)
					.show();
			return false;
		}
		return true;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.submitInputBtn) {
			if (verifyUserInput()) {

				if (mCouponArray == null) {
					mCouponArray = new String[2];
				}

				mCouponArray[0] = mCodeEdt.getText().toString();
				mCouponArray[1] = mSecretEdt.getText().toString();

				if (mTask != null) {
					mTask.cancel(true);
					mTask = null;
				}

				mTask = new GetStatusInfoTask();
				mTask.execute(mCouponArray[0]);
			}
		} else if (v.getId() == R.id.scanCode) {
			Intent intent = new Intent(VerifyCouponActivity.this,
					CaptureActivity.class);
			this.startActivityForResult(intent, 1);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1 && resultCode == RESULT_OK) {
			String result = data.getStringExtra(GlobalKey.COUPON_CODE);
			if (result == null)
				return;
			String[] parmas = result.split(",");
			if (parmas.length != 2) {
				Toast.makeText(this, getString(R.string.scanCode_failure),
						Toast.LENGTH_SHORT).show();
				return;
			}
			mCodeEdt.setText(parmas[0]);
			mSecretEdt.setText(parmas[1]);
		}
	}

	private final class DialogHolder {
		ImageView productImageView;
		TextView titleView;
		TextView detailView;
		TextView priceView;
		DiscountView marketPriceView;
		Button confuseButton;
		Button cancelButton;

		public DialogHolder(View rootView) {
			productImageView = (ImageView) rootView
					.findViewById(R.id.product_icon);
			titleView = (TextView) rootView.findViewById(R.id.title_text_view);
			detailView = (TextView) rootView
					.findViewById(R.id.detail_text_view);
			priceView = (TextView) rootView.findViewById(R.id.price_text_view);
			marketPriceView = (DiscountView) rootView
					.findViewById(R.id.discount_text_view);
			confuseButton = (Button) rootView.findViewById(R.id.confuse_button);
			cancelButton = (Button) rootView.findViewById(R.id.cancel_button);
		}

		public void setAcceptListener(OnClickListener listener) {
			confuseButton.setOnClickListener(listener);
		}

		public void setCancelListener(OnClickListener listener) {
			cancelButton.setOnClickListener(listener);
		}

		public void setData(ProductInfo item) {
			if (item == null)
				return;
			if (item.partner != null) {
				titleView.setText(item.partner.getTitle());
			} else {
				titleView.setText(item.product);
			}
			detailView.setText(item.title);
			priceView.setText(getString(R.string.format_team_buy,
					item.getTeamPrice()));
			marketPriceView.setText(getString(R.string.format_sale_price,
					item.getMarketPrice()));
			productImageView.setImageResource(R.drawable.default_pic_small);
			String imageUrl = item.getSmallImageUrl();
			ImageLoader.getInstance().displayImage(imageUrl, productImageView);
		}
	}
}
