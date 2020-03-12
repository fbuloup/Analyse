//
//  main.m
//  startAnalyse
//
//  Created by frank on 09/01/10.
//  Copyright __MyCompanyName__ 2010. All rights reserved.
//

#import <Cocoa/Cocoa.h>

int const RESTART_ANALYSE = 1;

int main(int argc, char *argv[])
{

	NSAutoreleasePool	 *autoreleasepool = [[NSAutoreleasePool alloc] init];

	// Création du NSTask "whereis"
	NSTask *whereis = [[NSTask alloc] init];
	[whereis autorelease];
	// Association de la commande "whereis" au NSTask en utilisant un NSString
	[whereis setLaunchPath:@"/usr/bin/whereis"];
	// Passage des différents arguments au travers d'un NSArray contenant des NSString
	[whereis setArguments:[NSArray arrayWithObjects:@"java",nil]];
 
	// Création du NSPipe qui récupérera le résultat
	NSPipe *outputPipe = [[NSPipe alloc] init];
	[outputPipe autorelease];
	[whereis setStandardOutput:outputPipe];
	// Création d'un NSFileHandle qui récupérera les informations du NSPipe
	NSFileHandle *outputFileHandle = [[outputPipe fileHandleForReading] retain];
	[outputFileHandle autorelease];

	// Exécution des NSTask
	[whereis launch];
	
	// Récupération du fichier de sortie dans un NSData
	NSData *outputData = [[outputFileHandle readDataToEndOfFile] retain];
	[outputData autorelease];
	// Transformation du NSData en NSString
	NSString *outputString = [[NSString alloc] initWithData:outputData encoding:NSUTF8StringEncoding];
	[outputString autorelease];
	// Affichage du résultat
	NSLog(@"Java path : %@", [outputString description]);
	
	BOOL javaIsPresent = ![outputString isEqualToString:@""];
	NSLog(@"Is path empty (Java not present) : %@", (javaIsPresent ? @"YES" : @"NO"));
	
	if(javaIsPresent) {
		NSString *analyseLauncherPath = [[[[NSBundle mainBundle] bundlePath] stringByDeletingLastPathComponent] stringByAppendingPathComponent:@"OSXAnalyseLauncher.sh"];
		[analyseLauncherPath autorelease];
		NSLog(@"OSXAnalyseLauncher.sh path : %@", [analyseLauncherPath description]);
		
		// Cr√©ation du NSTask "launchAnalyse"
		NSTask *launchAnalyse = [[NSTask alloc] init];
		[launchAnalyse autorelease];
		// Association de la commande "sh" au NSTask en utilisant un NSString
		[launchAnalyse setLaunchPath:@"/bin/sh"];
		// Passage des diff√©rents arguments au travers d'un NSArray contenant des NSString
		[launchAnalyse setArguments:[NSArray arrayWithObjects:@"-c",analyseLauncherPath,nil]];
		[launchAnalyse launch];
		
		/*[launchAnalyse waitUntilExit];
		int status = [launchAnalyse terminationStatus];
		NSLog(@"status exit : %d",status);
		if(status == RESTART_ANALYSE){			
			NSString *analyseLauncherPathApp = [[NSBundle mainBundle] bundlePath];
			[analyseLauncherPathApp autorelease];
			NSLog(@"Relaunch path : %@", [analyseLauncherPathApp description]);
			NSTask *reLaunchAnalyse = [[NSTask alloc] init];
			[reLaunchAnalyse autorelease];
			// Association de la commande "sh" au NSTask en utilisant un NSString
			[reLaunchAnalyse setLaunchPath:@"/usr/bin/open"];
			// Passage des diff√©rents arguments au travers d'un NSArray contenant des NSString
			[reLaunchAnalyse setArguments:[NSArray arrayWithObjects:@"-a",analyseLauncherPathApp,nil]];
			[reLaunchAnalyse launch];			
		}*/
		
		//Comment because freed already freed object error ?
		[autoreleasepool release];
		
		return 0;
		
	}
	
	[autoreleasepool release];
 
    return NSApplicationMain(argc,  (const char **) argv);
	
	
}
