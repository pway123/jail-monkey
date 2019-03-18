//
//  JailMonkey.m
//  Trackops
//
//  Created by Gant Laborde on 7/19/16.
//  Copyright © 2016 Facebook. All rights reserved.
//

#import "JailMonkey.h"
@import UIKit;

// For debugger detection
#import <dlfcn.h>
#import <sys/types.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/sysctl.h>
#include <unistd.h>

static NSString * const JMJailbreakTextFile = @"/private/jailbreak.txt";
static NSString * const JMisJailBronkenKey = @"isJailBroken";
static NSString * const JMCanMockLocationKey = @"canMockLocation";
static NSString * const JMisDebuggerAttachedKey = @"isDebuggerAttached";

typedef int (*ptrace_ptr_t)(int _request, pid_t _pid, caddr_t _addr, int _data);

#if !defined(PT_DENY_ATTACH)
#define PT_DENY_ATTACH 31
#endif  // !defined(PT_DENY_ATTACH)

@implementation JailMonkey

RCT_EXPORT_MODULE();

+ (BOOL)requiresMainQueueSetup
{
    return YES;
}

- (NSArray *)pathsToCheck
{
    return @[
             @"/Applications/Cydia.app",
             @"/Library/MobileSubstrate/MobileSubstrate.dylib",
             @"/bin/bash",
             @"/usr/sbin/sshd",
             @"/etc/apt",
             @"/private/var/lib/apt",
             @"/private/var/stash",
             @"/private/var/tmp/cydia.log",
             @"/private/var/lib/cydia",
             @"/private/var/mobile/Library/SBSettings/Themes",
             @"/Library/MobileSubstrate/MobileSubstrate.dylib",
             @"/Library/MobileSubstrate/DynamicLibraries/Veency.plist",
             @"/Library/MobileSubstrate/DynamicLibraries/LiveClock.plist",
             @"/System/Library/LaunchDaemons/com.ikey.bbot.plist",
             @"/System/Library/LaunchDaemons/com.saurik.Cydia.Startup.plist",
             @"/var/cache/apt",
             @"/var/lib/apt",
             @"/var/lib/cydia",
             @"/var/log/syslog",
             @"/var/tmp/cydia.log",
             @"/bin/sh",
             @"/usr/sbin/sshd",
             @"/usr/libexec/ssh-keysign",
             @"/usr/sbin/sshd",
             @"/usr/bin/sshd",
             @"/usr/libexec/sftp-server",
             @"/etc/ssh/sshd_config",
             @"/Applications/RockApp.app",
             @"/Applications/Icy.app",
             @"/Applications/WinterBoard.app",
             @"/Applications/SBSettings.app",
             @"/Applications/MxTube.app",
             @"/Applications/IntelliScreen.app",
             @"/Applications/FakeCarrier.app",
             @"/Applications/blackra1n.app"
             ];
}

- (NSArray *)schemesToCheck
{
    return @[
             @"cydia://package/com.example.package",
             ];
}

- (BOOL)isBlacklistedPaths:(NSArray *)pathArray
{
    BOOL existsPath = NO;
    NSError *errorObj;
    
    for(NSString *path in pathArray){
        NSDictionary<NSFileAttributeKey,id> *dict = [[NSFileManager defaultManager] attributesOfItemAtPath:path error:&errorObj];
        
        for(NSString *key in dict){
            //id value = dict[key];
            existsPath = YES;
        }
        
        if(errorObj.code==257){
            existsPath = YES;
        }
    }
 
    return existsPath;
}

- (BOOL)checkPaths
{
    BOOL existsPath = NO;
    
    for (NSString *path in [self pathsToCheck]) {
        if ([[NSFileManager defaultManager] fileExistsAtPath:path]){
            existsPath = YES;
            break;
        }
    }
    
    return existsPath;
}

- (BOOL)checkSchemes
{
    BOOL canOpenScheme = NO;
    
    for (NSString *scheme in [self schemesToCheck]) {
        if([[UIApplication sharedApplication] canOpenURL:[NSURL URLWithString:scheme]]){
            canOpenScheme = YES;
            break;
        }
    }
    
    return canOpenScheme;
}

- (BOOL)canViolateSandbox{
	NSError *error;
    BOOL grantsToWrite = NO;
	NSString *stringToBeWritten = @"This is an anti-spoofing test.";
	[stringToBeWritten writeToFile:JMJailbreakTextFile atomically:YES
						  encoding:NSUTF8StringEncoding error:&error];
	if(!error){
		//Device is jailbroken
		grantsToWrite = YES;
	}
    
    [[NSFileManager defaultManager] removeItemAtPath:JMJailbreakTextFile error:nil];
    
    return grantsToWrite;
}

- (BOOL)isJailBroken{
    NSArray* pathToCheck = [self pathsToCheck];
    return [self checkPaths] || [self checkSchemes] || [self canViolateSandbox] || [self isBlacklistedPaths:pathToCheck];
}

- (NSDictionary *)constantsToExport
{
	return @{
			 JMisJailBronkenKey: @(self.isJailBroken),
			 JMCanMockLocationKey: @(self.isJailBroken)
			 };
}

@end
