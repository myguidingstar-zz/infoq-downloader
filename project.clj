(defproject infoq-downloader "0.1.0-SNAPSHOT"
  :description "Download talks from Infoq to view offline"
  :url "http://github.com/myguidingstar/infoq-downloader"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :plugins [[lein-cl2c "0.0.1-SNAPSHOT"]]
  :profiles
  {:dev
   {:eval-in :nrepl
    :dependencies
    [[chlorine/repl "0.1.0-SNAPSHOT"]]
    :repl-options {:nrepl-middleware [chlorine.repl/wrap-chlorine-repl]}}}
  :cl2c {:compile
         {:watch ["src", "test"]
          :filter "src/"
          :path-map ["src/" => "lib/"]
          :paths ["node_modules/"]
          :strategy "dev"
          ;; some files may take too long to compile. We need a limit
          :timeout 2000}})
