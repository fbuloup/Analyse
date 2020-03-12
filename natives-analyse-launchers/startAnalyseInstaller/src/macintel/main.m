//
//  main.m
//  startAnalyse
//
//  Created by frank on 09/01/10.
//  Copyright __MyCompanyName__ 2010. All rights reserved.
//

#import <Cocoa/Cocoa.h>

int const START_ANALYSE = 1;

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
	NSLog(@"Is path empty (Java not present) : %@", (javaIsPresent ? @"NOP" : @"YEP"));
	
	if(javaIsPresent) {
		
		NSString *analyseInstallerLauncherPath = [[NSString alloc] initWithString:@"java -XstartOnFirstThread -jar "];
		[analyseInstallerLauncherPath autorelease];
		analyseInstallerLauncherPath = [analyseInstallerLauncherPath  stringByAppendingString:[[[[NSBundle mainBundle] bundlePath] stringByDeletingLastPathComponent] stringByAppendingPathComponent:@"OSXintel-analyse-installer.jar"]];		
		NSLog(@"analyse-installer command : %@", [analyseInstallerLauncherPath description]);
		
		// Création du NSTask "launchAnalyse"
		NSTask *launchAnalyseInstaller = [[NSTask alloc] init];
		[launchAnalyseInstaller autorelease];
		// Association de la commande "sh" au NSTask en utilisant un NSString
		[launchAnalyseInstaller setLaunchPath:@"/bin/sh"];
		// Passage des différents arguments au travers d'un NSArray contenant des NSString
		[launchAnalyseInstaller setArguments:[NSArray arrayWithObjects:@"-c",analyseInstallerLauncherPath,nil]];
		[launchAnalyseInstaller launch];
		/*[launchAnalyseInstaller waitUntilExit];
		int status = [launchAnalyseInstaller terminationStatus];
		if(status == START_ANALYSE){
			NSString* cmdString = [NSString stringWithContentsOfFile:[NSHomeDirectory() stringByAppendingPathComponent:@"startAnalyseCmde.tmp"] encoding:NSUTF8StringEncoding error:NULL];
			cmdString = [cmdString stringByAppendingPathComponent:@"startAnalyse.app/Contents/MacOS/startAnalyse"];
			[cmdString autorelease];
			NSLog(@"Cmd string to launch Analyse : %@", [cmdString description]);
			NSTask *launchAnalyse = [[NSTask alloc] init];
			[launchAnalyse autorelease];
			// Association de la commande "sh" au NSTask en utilisant un NSString
			[launchAnalyse setLaunchPath:@"/usr/bin/open"];
			// Passage des différents arguments au travers d'un NSArray contenant des NSString
			[launchAnalyse setArguments:[NSArray arrayWithObjects:@"-a",cmdString,nil]];
			[launchAnalyse launch];
			NSTask *rmFile = [[NSTask alloc] init];
			[rmFile autorelease];
			[rmFile setLaunchPath:@"/bin/rm"];
			[rmFile setArguments:[NSArray arrayWithObjects:@"-f",[NSHomeDirectory() stringByAppendingPathComponent:@"startAnalyseCmde.tmp"],nil]];
			[rmFile launch];

		}*/
		[autoreleasepool release];
		
		return 0;
		
	}
	
	[autoreleasepool release];
 
    return NSApplicationMain(argc,  (const char **) argv);
	
	
}
