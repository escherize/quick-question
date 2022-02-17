#! /opt/homebrew/bin/bb

(ns qq
  (:require [babashka.tasks :refer [shell]]
            [babashka.fs :as fs]
            [cheshire.core :as json]
            [selmer.parser :refer [<<]]
            [clojure.pprint :as pprint]
            [clojure.set :as set]
            [clojure.string :as str]))

(def env (into {} (System/getenv)))
(when (get env "DEBUG") (println "loading"))

(defn qq!
  "Takes in questions which is a single or a collection of maps which get
  processed by enquire.js as prompts.

  References:
  - for the enquire.js prompt types:
  https://github.com/enquirer/enquirer#-built-in-prompts
  - the names of `type`s:
  https://github.com/enquirer/enquirer/blob/8d626c206733420637660ac7c2098d7de45e8590/lib/prompts/index.js
  "
  [questions]
  (let [seed (str
               (apply str (repeatedly 10 #(rand-nth "qwertyuioplkjhgfdsazxcvbnmPOIUYTREWQASDFGHJKLMNBVCXZ")))
               "_")
        in-file  (str seed "in.json")
        out-file (str seed "out.json")
        json-questions (json/encode questions)]
    (try (let [_ (spit in-file json-questions)
               _ (shell (<< "node qq.js {{in-file}} {{out-file}}"))
               out (loop [] (if (fs/exists? out-file) (slurp out-file) (recur)))]
           (json/decode out
                        (comp keyword
                              (fn [s] (str/replace s #"\s+" "-")))))
         (finally (do
                    (fs/delete in-file)
                    (fs/delete out-file))))))

#_(prn (qq! [{:type :input :name :name :message "What is your name"}
             {:type :numeral :name :age :message "What is your age?"}]))

(def ^:private fill-map-docs
  {["input" :initial] "This is also the default value."})

(defn fill-map [m]
  (into {}
        (for [[kkey field] m]
          (qq! {:type field
                :name (name kkey)
                :message (str "set the " field " value for " kkey
                              (when-let [doc (fill-map-docs [field kkey])]
                                (str " (" doc " )")))}))))

(def type->fields
  {"autocomplete" {:limit "numeral"
                   :initial "numeral"
                   :choices "list"}
   "basicauth" {:username "input"
                :password "password"
                :showPassword "confirm"}
   "confirm" {}
   ;; "form" {} ;; TODO
   "input" {:initial "input"}
   "invisible" {}
   "list" {}
   "multiselect" {:choices "list"}
   "numeral" {}
   "password" {}
   "quiz" {:choices "list"
           :correctChoice "numeral"}
   ;; "survey" {} ;; TODO
   ;; "scale" {} ;; TODO
   "select" {:choices "list"}
   "sort" {:hint "input"
           :numbered "confirm"
           :choices "list"}
   ;; "snippet" {} ;; TODO
   "toggle" {:enabled "input"
             :disabled "input"}

   })

(def question-types (sort (keys type->fields)))

(defn create-questions []
  (let [*questions (atom [])
        *done? (atom false)]
    (while (not @*done?)
      (let [{:keys [type name msg]
             :as question-data} (qq! [{:type "autocomplete"
                                       :name :type
                                       :message "What question type do you want to add?"
                                       :choices question-types}
                                      {:type "input"
                                       :name :name
                                       :message "What name do you want to use? (this will be the key as well)."}
                                      {:type "input"
                                       :name :msg
                                       :message "And the message?"}])
            q (merge
                (fill-map (get type->fields type))
                {:name name
                 :type type
                 :message msg})]
        (swap! *questions conj q)
        (reset! *done? (:done (qq! {:type "toggle"
                                    :message "Done?"
                                    :enabled "Yes I'm done."
                                    :disabled "No, let me add more."
                                    :name :done})))))
    @*questions))


(defn tutorial []
    (println "Quick Question -- qq!")
    (newline)
    (println "Follow along with the prompts, to create, and then fill out your own prompt.")
    (println "Yes this is a prompt that generates a prompt. It's self-documenting.")
    (newline)
    (println "Also, please note, there are other form types but they are more complicated to generate.")
    (println "- docs for the enquire.js prompt types: https://github.com/enquirer/enquirer#-built-in-prompts")
    (println "- docs the names of types: https://github.com/enquirer/enquirer/blob/8d626c206733420637660ac7c2098d7de45e8590/lib/prompts/index.js")
    (newline)
    (println "Let's begin.")
    (newline)
    (newline)
    (flush)
    (let [questions (create-questions)]
      (newline)
      (println "Here are Your questions:")
      (newline)
      (pprint/pprint questions)
      (newline)
      (newline)
      (println "Now, we will ask you them, as if you called: ")
      (println "(qq! " (pr-str questions) ")")
      (newline)
      (newline)
      (flush)
      (qq! questions)))

(when (= *file* (System/getProperty "babashka.file"))
  (if (seq (set/intersection #{"-h" "--help"} (set *command-line-args*)))
    (tutorial)
    (println (pr-str (first *command-line-args*)))))
