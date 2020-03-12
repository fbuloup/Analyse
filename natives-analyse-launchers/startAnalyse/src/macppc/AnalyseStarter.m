#import "AnalyseStarter.h"

@implementation AnalyseStarter

- (IBAction)editURL:(id)sender
{
	NSLog(@"Goto java site !");
	[[NSWorkspace sharedWorkspace] openURL:[NSURL URLWithString:@"http://www.java.com/fr/download/manual.jsp"]];
}

- (IBAction)exit:(id)sender
{
	NSLog(@"Terminate");
	[NSApp terminate:self];
}

@end
