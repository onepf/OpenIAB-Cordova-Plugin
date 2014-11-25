OpenIAB-Cordova-Plugin
=======================

OpenIAB plugin for Cordova-based frameworks

Cordova Integration
-------------------

1) Add Android platform to your project using CLI.
```
$ cordova platform add android
```

2) Add OpenIAB plugin, referencing repo.
```
$ cordova plugin add https://github.com/onepf/OpenIAB-Cordova-Plugin.git
```

3) Build the app.
```
$ cordova build
```

4) Run it.
```
$ cordova run android
```

Intel XDK Integration
---------------------

![Import plugin](http://take.ms/Fc5Aa)

![Import settings](http://take.ms/JGni6)

![Import settings](http://take.ms/mluph)


Plugin Usage
------------

1) Include script to your application.
```
<script type="text/javascript" src="cordova.js"></script>
```

2) Simply call ```openiab``` object methods, passing callback functions.

a) Map you SKUs. This step is optional.
```
openiab.mapSku(function(){}, function(error){}, SKU1, openiab.STORE_NAME.GOOGLE, "sku_product");
```

b) Set some options.
```
openiab.options.storeKeys = [ [openiab.STORE_NAME.GOOGLE, 'your public key'] ];
openiab.options.availableStoreNames = [ openiab.STORE_NAME.GOOGLE, openiab.STORE_NAME.YANDEX ];
openiab.options.storeSearchStrategy = openiab.STORE_SEARCH_STRATEGY.INSTALLER_THEN_BEST_FIT;
```

c) Initialize plugin.
```
openiab.init(function(){}, function(error){}, [ "SKU1", "SKU2", "SKU3" ]);
```

d) Start purchase.
```
openiab.purchaseProduct(function(purchase){}, function(error){}, "SKU");
```

e) Consume consumable SKUs in order to be able to purchase it again.
```
openiab.consume(function(purchase){}, function(error){}, "SKU");
```

3) Use additional methods to get information about SKUs and purchases.

a) Can be used any time after ```init``` is finished.
```
openiab.getPurchases(function(purchaseList){}, function(error){});
```

b) Get details of the single SKU.
```
openiab.getSkuDetails(function(skuDetails){}, function(error){}, "SKU");
```

c) Get details of the SKU list.
```
openiab.getSkuListDetails(function(skuDetailsList){}, function(error){}, ["SKU1", "SKU2", "SKU3"]);
```

Also consider to check [sample application](https://github.com/GrimReio/OpenIAB-Cordova-sample) for cordova or xdk.

Data Structures
---------------

```
purchase =
{
    itemType:'',
    orderId:'',
    packageName:'',
    sku:'',
    purchaseTime:0,
    purchaseState:0,
    developerPayload:'',
    token:'',
    originalJson:'',
    signature:'',
    appstoreName:''
}

skuDetails =
{
  	itemType:'',
    sku:'',
    type:'',
    price:'',
    title:'',
    description:'',
    json:''
}

error =
{
    code:-1,
	message:''
}
```
