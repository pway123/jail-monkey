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
             ];
}

- (NSArray *)schemesToCheck
{
    return @[
             @"cydia://package/com.example.package",
             ];
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
    return [self checkPaths] || [self checkSchemes] || [self canViolateSandbox];
}

/*
 * Ref: https://github.com/vtky/ios-antidebugging/blob/master/antidebugging/main.m
*/
- (BOOL)checkPtrace{
  void* handle = dlopen(0, RTLD_GLOBAL | RTLD_NOW);
  ptrace_ptr_t ptrace_ptr = dlsym(handle, "ptrace");
  ptrace_ptr(PT_DENY_ATTACH, 0, 0, 0);
  dlclose(handle);
  
  return NO;
}

/*
 * Ref: https://github.com/vtky/ios-antidebugging/blob/master/antidebugging/main.m
*/
- (BOOL)checkSysctl{
  int mib[4];
  struct kinfo_proc info;
  size_t info_size = sizeof(info);

  // Initialize the flags so that, if sysctl fails for some bizarre
  // reason, we get a predictable result.

  info.kp_proc.p_flag = 0;

  // Initialize mib, which tells sysctl the info we want, in this case
  // we're looking for information about a specific process ID.

  mib[0] = CTL_KERN;
  mib[1] = KERN_PROC;
  mib[2] = KERN_PROC_PID;
  mib[3] = getpid();

  // Call sysctl.

  if (sysctl(mib, 4, &info, &info_size, NULL, 0) == -1)
  {
      perror("perror sysctl");
      exit(-1);
  }

  // We're being debugged if the P_TRACED flag is set.
  
  return ((info.kp_proc.p_flag & P_TRACED) != 0);
}

- (BOOL)isDebuggerAttached{
    return [self checkPtrace] || [self checkSysctl];
}

- (NSDictionary *)constantsToExport
{
	return @{
			 JMisJailBronkenKey: @(self.isJailBroken),
			 JMCanMockLocationKey: @(self.isJailBroken),
			 JMisDebuggerAttachedKey: @(self.isDebuggerAttached)
			 };
}

@end
