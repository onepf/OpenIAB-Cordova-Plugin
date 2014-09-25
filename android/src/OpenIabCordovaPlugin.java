package org.onepf.openiab.cordova;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.onepf.oms.OpenIabHelper;
import org.onepf.oms.appstore.googleUtils.Inventory;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.squareup.okhttp.internal.StrictLineReader;

import org.onepf.oms.SkuManager;
import org.onepf.oms.appstore.googleUtils.*;

import java.util.ArrayList;
import java.util.List;

public class OpenIabCordovaPlugin extends CordovaPlugin
{
    public static final String TAG = "OpenIAB-CordovaPlugin";

    public static final int RC_REQUEST = 10001; /**< (arbitrary) request code for the purchase flow */

    private OpenIabHelper _helper;
    private Inventory _inventory;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException
    {
        if ("init".equals(action))
        {
            JSONObject j = args.getJSONObject(0);
            boolean checkInventory = j.getBoolean("checkInventory");
            int checkInventoryTimeout = j.getInt("checkInventoryTimeout");
            int discoveryTimeout = j.getInt("discoveryTimeout");
            int verifyMode = j.getInt("verifyMode");

            OpenIabHelper.Options.Builder builder = new OpenIabHelper.Options.Builder()
                    .setCheckInventory(checkInventory)
                    .setCheckInventoryTimeout(checkInventoryTimeout)
                    .setDiscoveryTimeout(discoveryTimeout)
                    .setVerifyMode(verifyMode);

            JSONArray availableStores = j.getJSONArray("availableStores");
            for (int i = 0; i < availableStores.length(); ++i) {
                JSONArray pair = availableStores.getJSONArray(i);
                builder.addStoreKey(pair.get(0).toString(), pair.get(1).toString());
            }

            JSONArray prefferedStoreNames = j.getJSONArray("preferredStoreNames");
            for (int i = 0; i < prefferedStoreNames.length(); ++i) {
                builder.addPreferredStoreName(prefferedStoreNames.get(i).toString());
            }

            List<String> skuList = new ArrayList<String>();
            if (args.length() > 1) {
                JSONArray jSkuList = args.getJSONArray(1);
                int count = jSkuList.length();
                for (int i = 0; i < count; ++i) {
                    skuList.add(jSkuList.getString(i));
                }
            }
            init(builder.build(), skuList, callbackContext);
            return true;
        }
        else if ("purchaseProduct".equals(action))
        {
            String sku = args.getString(0);
            String payload = args.length() > 1 ? args.getString(1) : "";
            purchaseProduct(sku, payload, callbackContext);
            return true;
        }
        else if ("purchaseSubscription".equals(action))
        {
            String sku = args.getString(0);
            String payload = args.length() > 1 ? args.getString(1) : "";
            purchaseProduct(sku, payload, callbackContext);
            return true;
        }
        else if ("consume".equals(action))
        {
            String sku = args.getString(0);
            consume(sku, callbackContext);
            return true;
        }
        else if ("getSkuDetails".equals(action))
        {
            String sku = args.getString(0);
            getSkuDetails(sku, callbackContext);
            return true;
        }
        else if ("mapSku".equals(action))
        {
            String sku = args.getString(0);
            String storeName = args.getString(1);
            String storeSku = args.getString(2);
            mapSku(sku, storeName, storeSku);
            return true;
        }
        return false;  // Returning false results in a "MethodNotFound" error.
    }

    private void mapSku(String sku, String storeName, String storeSku) {
        SkuManager.getInstance().mapSku(sku, storeName, storeSku);
    }

    private void getSkuDetails(String sku, final CallbackContext callbackContext) {
        if (!checkInitialized(callbackContext)) return;

        if (!_inventory.hasDetails(sku)) {
            callbackContext.error(Serialization.errorToJson(-1, "SkuDetails not found: " + sku));
            return;
        }

        JSONObject jsonSkuDetails;
        try {
            jsonSkuDetails = Serialization.skuDetailsToJson(_inventory.getSkuDetails(sku));
        } catch (JSONException e) {
            callbackContext.error(Serialization.errorToJson(-1, "Couldn't serialize SkuDetails: " + sku));
            return;
        }
        callbackContext.success(jsonSkuDetails);
    }

    private void init(final OpenIabHelper.Options options, final List<String> skuList, final CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                _helper = new OpenIabHelper(cordova.getActivity(), options);
                createBroadcasts();

                // Start setup. This is asynchronous and the specified listener
                // will be called once setup completes.
                Log.d(TAG, "Starting setup.");
                _helper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                    public void onIabSetupFinished(IabResult result) {
                        Log.d(TAG, "Setup finished.");

                        if (result.isFailure()) {
                            // Oh noes, there was a problem.
                            Log.e(TAG, "Problem setting up in-app billing: " + result);
                            callbackContext.error(Serialization.errorToJson(result));
                            return;
                        }

                        Log.d(TAG, "Querying inventory.");
                        // TODO: this is SHIT! product and subs skus shouldn't be sent two times
                        _helper.queryInventoryAsync(true, skuList, skuList, new BillingCallback(callbackContext));
                    }
                });
            }
        });
    }

    private boolean checkInitialized(final CallbackContext callbackContext) {
        if (_helper == null || _inventory == null)
        {
            Log.e(TAG, "Not initialized");
            callbackContext.error("Not initialized");
            return false;
        }
        return true;
    }

    private void purchaseProduct(final String sku, final String developerPayload, final CallbackContext callbackContext) {
        if (!checkInitialized(callbackContext)) return;

        Log.d(TAG, "SKU: " + SkuManager.getInstance().getStoreSku(OpenIabHelper.NAME_GOOGLE, sku));

        cordova.setActivityResultCallback(this);
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                _helper.launchPurchaseFlow(cordova.getActivity(), sku, RC_REQUEST, new BillingCallback(callbackContext), developerPayload);
            }
        });
    }

    public void purchaseSubscription(final String sku, final String developerPayload, final CallbackContext callbackContext) {
        if (!checkInitialized(callbackContext)) return;

        cordova.setActivityResultCallback(this);
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                _helper.launchSubscriptionPurchaseFlow(cordova.getActivity(), sku, RC_REQUEST, new BillingCallback(callbackContext), developerPayload);
            }
        });
    }

    private void consume(final String sku, final CallbackContext callbackContext) {
        if (!checkInitialized(callbackContext)) return;

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!_inventory.hasPurchase(sku))
                {
                    callbackContext.error(Serialization.errorToJson(-1, "Product haven't been purchased: " + sku));
                    return;
                }

                Purchase purchase = _inventory.getPurchase(sku);
                _helper.consumeAsync(purchase, new BillingCallback(callbackContext));
            }
        });
    }

    /**
     * Callback class for when a purchase or consumption process is finished
     */
    public class BillingCallback implements
            IabHelper.QueryInventoryFinishedListener,
            IabHelper.OnIabPurchaseFinishedListener,
            IabHelper.OnConsumeFinishedListener {

        final CallbackContext _callbackContext;

        public BillingCallback(final CallbackContext callbackContext) {
            _callbackContext = callbackContext;
        }

        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory process finished.");
            if (result.isFailure()) {
                _callbackContext.error(Serialization.errorToJson(result));
                return;
            }

            Log.d(TAG, "Query inventory was successful. Init finished.");
            _inventory = inventory;
            _callbackContext.success();
        }

        @Override
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase process finished: " + result + ", purchase: " + purchase);
            if (result.isFailure()) {
                Log.e(TAG, "Error purchasing: " + result);
                _callbackContext.error(Serialization.errorToJson(result));
                return;
            }
            _inventory.addPurchase(purchase);
            Log.d(TAG, "Purchase successful.");
            JSONObject jsonPurchase;
            try {
                jsonPurchase = Serialization.purchaseToJson(purchase);
            } catch (JSONException e) {
                _callbackContext.error(Serialization.errorToJson(-1, "Couldn't serialize the purchase"));
                return;
            }
            _callbackContext.success(jsonPurchase);
        }

        @Override
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            Log.d(TAG, "Consumption process finished. Purchase: " + purchase + ", result: " + result);

            if (result.isFailure()) {
                Log.e(TAG, "Error while consuming: " + result);
                _callbackContext.error(Serialization.errorToJson(result));
                return;
            }
            _inventory.erasePurchase(purchase.getSku());
            Log.d(TAG, "Consumption successful. Provisioning.");
            JSONObject jsonPurchase;
            try {
                jsonPurchase = Serialization.purchaseToJson(purchase);
            } catch (JSONException e) {
                _callbackContext.error(Serialization.errorToJson(-1, "Couldn't serialize the purchase"));
                return;
            }
            _callbackContext.success(jsonPurchase);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + ", " + resultCode + ", " + data);

        // Pass on the activity result to the helper for handling
        if (!_helper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }

    private void createBroadcasts() {
        Log.d(TAG, "createBroadcasts");
        IntentFilter filter = new IntentFilter(YANDEX_STORE_ACTION_PURCHASE_STATE_CHANGED);
        cordova.getActivity().registerReceiver(_billingReceiver, filter);
    }

    private void destroyBroadcasts() {
        Log.d(TAG, "destroyBroadcasts");
        try {
            cordova.getActivity().unregisterReceiver(_billingReceiver);
        } catch (Exception ex) {
            Log.d(TAG, "destroyBroadcasts exception:\n" + ex.getMessage());
        }
    }

    // Yandex specific
    public static final String YANDEX_STORE_SERVICE = "com.yandex.store.service";
    public static final String YANDEX_STORE_ACTION_PURCHASE_STATE_CHANGED = YANDEX_STORE_SERVICE + ".PURCHASE_STATE_CHANGED";

    private BroadcastReceiver _billingReceiver = new BroadcastReceiver() {
        private static final String TAG = "YandexBillingReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive intent: " + intent);

            if (YANDEX_STORE_ACTION_PURCHASE_STATE_CHANGED.equals(action)) {
                purchaseStateChanged(intent);
            }
        }

        private void purchaseStateChanged(Intent data) {
            Log.d(TAG, "purchaseStateChanged intent: " + data);
            _helper.handleActivityResult(RC_REQUEST, Activity.RESULT_OK, data);
        }
    };
}