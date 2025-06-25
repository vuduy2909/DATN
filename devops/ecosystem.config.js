/* ecosystem.config.js */
module.exports = {
    apps: [
      {
        /* ---------------------------------
         *  Main settings
         * ---------------------------------*/
        name: "spring-app",          // PM2 process name
        script: "java",              // Executable to run
        args: "-jar libs/wellness-navigator-gym-0.0.1-SNAPSHOT.jar", // Full path to your jar
        interpreter: "none",         // Tell PM2 “do NOT use node”
  
        /* ---------------------------------
         *  Instance / restart behaviour
         * ---------------------------------*/
        exec_mode: "fork",           // One JVM = one process
        instances: 1,                // Or use cluster mode if you run multiple pods
        autorestart: true,           // Restart on crash/exit codes !=0
        watch: ["libs/wellness-navigator-gym-0.0.1-SNAPSHOT.jar"],
        max_memory_restart: "1G",    // JVM killed & restarted if it eats >1 GiB
      }
    ]
  };
  