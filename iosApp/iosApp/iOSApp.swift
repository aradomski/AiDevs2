import SwiftUI
import ComposeApp

@main
struct iOSApp: App {

    init() {
        InitKt.doInit()
        KoinKt.doInitKoin()
        
        }

	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}
