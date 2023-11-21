
#if __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#else
#import <React/RCTBridgeModule.h>
#endif

#if __has_include("RCTReloadCommand.h")
#import "RCTReloadCommand.h"
#else
#import <React/RCTReloadCommand.h>
#endif


@class RNGattServer;
@interface RNGattServer : NSObject <RCTBridgeModule>

@end
