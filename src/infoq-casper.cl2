(def iPad-user-agent
  (str "Mozilla/5.0 (iPad; U; CPU OS 4_2_1 like Mac OS X; en-us) "
       "AppleWebKit/533.17.9 (KHTML, like Gecko) "
       "Version/5.0.2 "
       "Mobile/8C148 "
       "Safari/6533.18.5"))

(def iPhone-user-agent
  (str "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_2_1 like Mac OS X; da-dk) "
       "AppleWebKit/533.17.9 (KHTML, like Gecko) "
       "Version/5.0.2 "
       "Mobile/8C148 "
       "Safari/6533.18.5"))

(def casper-config
  {:client-scripts []
   :page-settings
   {:load-images false
    :load-plugins false
    :user-agent iPhone-user-agent}})

(require fs)

(defn spit [file content]
  (fs.write file content :w))

(defn slurp [file]
  (fs.read file))

(defn get-contents
  "Runs in InfoQ page's context to get data.
Returns the data as a vector."
  []
  ;; casperjs seems to throw exception when evaluation result contains null
  ;; Use "" in those cases
  [(or document.title "")
   ;; javascript vars found in page body
   slides times ;; TIMES
   (or (.. document
           (querySelector "video > source")
           (getAttribute "src"))
       "")])

(def casper (. (require* "casper")
               (create casper-config)))

(def url (first casper.cli.args))

(if-not url
  (do (casper.echo "Error: No URL specified" :WARNING)
      (casper.echo "Usage:" :COMMENT)
      (casper.echo
       (str "casper infoq-casper.js"
            " http://www.infoq.com/presentations/a-presentation-name")
       :PARAMETER)
      (casper.exit 1))
  (console.log (str "Opening: " url)))

(defn params->presentz
  "Given data fetched from InfoQ page context, generate JSON data
to be included in local presentz HTML file."
  [title local-slide-urls times local-video-url]
  (let [slides
        (for [[i t] times]
          {:url (nth local-slide-urls i)
           :time t})]
    {:title title
     :chapters [{:title title
                 ;; :duration is not needed
                 :video {:url local-video-url}
                 :slides slides}]}))

(defn prepare
  "Prepare a directory for each presentation."
  [title]
  (if (fs.exists title)
    (console.log (str "Directory for presentation " title
                      " exists. Nothing to prepare."))
    (do
      (console.log (str "Preparing a directory for presentation "
                        title))
      (fs.make-directory title)
      (fs.make-directory (str title "/slides"))
      (fs.copy-tree "assets" (str title "/assets")))))

(defn merge-data
  "Merge JSON data into presentz HTML template."
  [content data]
  (.replace content "{{presentation-data}}" data))

(defn merge-title
  "Merge page title into presentz HTML template."
  [title content]
  (.replace content "{{title}}" title))

(defn url->name [url]
  (last (.split url "/")))

(defn url->file-extension [url]
  (last (.split url ".")))

(defn slide->local
  "Local path for slide image."
  [slide-url]
  (str "slides/" (url->name slide-url)))

(defn video->aria2-lines
  "Lines to include in arial2c input file to download a given video."
  [video-url title]
  (str video-url "
  dir=" title))

(defn slide->aria2-lines
  "Lines to include in arial2c input file to download a given slide."
  [slide-url title]
  (str "http://www.infoq.com" slide-url "
  dir=" title "/slides"))

(defn generate-aria2 [title slide-urls video-url]
  (console.log "Generating aria2.txt...")
  (-> (for [[i slide] slide-urls
            :let [id (+ 1 i)]]
        (slide->aria2-lines slide title))
      (conj (video->aria2-lines video-url title))
      (.join "\n")
      (#(spit (str title "/aria2.txt") %))))

(defn generate-html [title slide-urls video-url times page-title]
  (console.log (str "Generating HTML file..."))
  (->> (params->presentz title
                         (map #(slide->local %) slide-urls)
                         times
                         (url->name video-url))
       JSON.stringify
       (merge-data (slurp "template.html"))
       (merge-title page-title)
       (spit (str title "/index.html"))))

(. casper start url
   (fn []
     (let [[page-title slide-urls times video-url]
           (this.evaluate get-contents)
           title (url->name url)
           page-title (or page-title title)
           video-url (or video-url "unknown.video.url")]
       (console.log (str "Entering " title "..."))
       (when (= video-url "unknown.video.url")
         (casper.echo "Unknown video url. You need to figure it out yourself"))
       (prepare title)
       (generate-aria2 title slide-urls video-url)
       (generate-html title slide-urls video-url times page-title)
       (casper.echo
        "You can now run aria2c to download slides and video with:" :INFO)
       (casper.echo "(download resume is enabled)" :COMMENT)
       (casper.echo (str "aria2c -x 16 --auto-file-renaming=false -i "
                         title
                         "/aria2.txt")
                    :PARAMETER))))

(. casper run)
