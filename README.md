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

a) Map you SKU.
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
openiab.init(function(){}, function(error){}, [ SKU1, SKU2, SKU3 ]);
```
d) Start purchase.
```
openiab.purchaseProduct(function(purchase){}, function(error){}, SKU1);
```

Also consider to check [sample application](https://github.com/GrimReio/OpenIAB-Cordova-sample) for cordova or xdk.
