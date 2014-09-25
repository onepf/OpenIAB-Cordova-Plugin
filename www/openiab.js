var exec = require('cordova/exec');

PLUGIN = "OpenIabCordovaPlugin";

function OpenIAB()
{
	this.VERIFY_MODE =
	{
		EVERYTHING:0,
		SKIP:1,
		ONLY_KNOWN:2
	}

	this.STORE_NAME =
	{
	    GOOGLE:"com.google.play",
	    AMAZON:"com.amazon.apps",
	    SAMSUNG:"com.samsung.apps",
	    YANDEX:"com.yandex.store",
	    NOKIA:"com.nokia.nstore",
	    APPLAND:"Appland",
	    SLIDEME:"SlideME",
	    APTOIDE:"cm.aptoide.pt"
	}

	this.options = 
	{
		checkInventory:false,
		discoveryTimeout:5 * 1000,
		checkInventoryTimeout:10 * 1000,
		verifyMode:this.VERIFY_MODE.SKIP,
		preferredStoreNames: [ this.STORE_NAME.GOOGLE, this.STORE_NAME.YANDEX ],
		availableStores: [ [this.STORE_NAME.GOOGLE, 'public_key'] ]
	}

	this.error =
	{
		code:-1,
		message:""
	}

	this.purchase =
	{
        itemType:null,
        orderId:null,
        packageName:null,
        sku:null,
        purchaseTime:null,
        purchaseState:null,
        developerPayload:null,
        token:null,
        originalJson:null,
        signature:null,
        appstoreName:null
	}

	this.skuDetails =
	{
		itemType:null,
        sku:null,
        type:null,
        price:null,
        title:null,
        description:null,
        json:null
	}
}

OpenIAB.prototype.mapSku = function(success, error, sku, storeName, storeSku)
{
	exec(success, error, PLUGIN, "mapSku", [sku, storeName, storeSku]);
}

OpenIAB.prototype.getSkuDetails = function(success, error, sku)
{
	exec(success, error, PLUGIN, "getSkuDetails", [sku]);
}

OpenIAB.prototype.init = function(success, error, skuList)
{
	exec(success, error, PLUGIN, "init", [this.options, skuList]);
}

OpenIAB.prototype.purchaseProduct = function(success, error, sku, payload)
{
	exec(success, error, PLUGIN, "purchaseProduct", [sku, payload]);
}

OpenIAB.prototype.purchaseSubscription = function(success, error, sku, payload)
{
	exec(success, error, PLUGIN, "purchaseSubscription", [sku, payload]);
}

OpenIAB.prototype.consume = function(success, error, sku)
{
	exec(success, error, PLUGIN, "consume", [sku]);
}

module.exports = new OpenIAB();