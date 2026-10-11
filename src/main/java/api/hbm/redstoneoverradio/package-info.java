/**
 * INDEX: the outward interface for Redstone-over-Radio.
 *
 * The three faces here have separate jobs: IRORInfo only reports which functions it knows (no point implementing it alone, the other two inherit it);
 * IRORValueProvider reads values by name and never changes state; IRORInteractive runs commands and usually changes it.
 *
 * Signals are strings throughout - simpler to serialise and to store in NBT; want an int, run it through parseInt yourself.
 *
 * Implementations usually fall into four flashlight types: readers (broadcast some named values), controllers (take commands and change themselves),
 * programmers (send and receive on a frequency plus commands, most capable but hog the band), and logic receivers (drive redstone after a compare/match).
 * Also, the RTTY system itself can carry arbitrary objects; INDEX sticks to strings.
 */
package api.hbm.redstoneoverradio;

/*

  __     __      __     _________      ________     __    __           __        __       ______     __
 /_/|   /_/\    /_/|   /________/\    /_______/|   /_/|  /_/|         /_/|_____ /_/|     /_____/|   /_/|
|  ||  |   \\  |  ||  |   ___   \ \  |  ______|/  |  |\_|  ||        |  |/_____|  ||    |___   ||  |  ||
|  ||  |  \ \\ |  ||  |  ||  \   \/  |  ||___     |  \/_/  |/        |   ______   ||     /__|  ||  |  ||__
|  ||  |  |\ \\|  ||  |  ||   \  ||  |  |/__/|     \      //         |  |/_____|  ||    |   ___|/  |  |/_/|
|  ||  |  ||\ \|  ||  |  ||    | ||  |  ____|/      >    </\         |____    ____|/_   |  |/__/|  |   __|/
|  ||  |  || \ |  ||  |  ||___/  |/  |  ||_____    /  __  \/|       /_____|  |______/|  |______|/  |  ||
|  ||  |  ||  \   ||  |  |/__/   /   |  |/____/|  |  /| \  ||      |________________|/             |  ||
|__|/  |__|/   \__|/  |_________/    |________|/  |__|/ |__|/                                      |__|/

(not an index - INDEX is just a name)
*/
