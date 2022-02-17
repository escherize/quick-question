# quick-question

> A babashka script (and installer) that lets you use the beautiful [enquirer.js](https://github.com/enquirer) but with our superior edn data.

## installation

I would copy this into some init script, maybe into your babashka preambles?

`install.clj`
``` clojure
#! /opt/homebrew/bin/bb

(ns install
  (:require [babashka.deps :as deps]
            [babashka.fs :as fs]
            [babashka.tasks :refer [shell]]))

(defmacro install!
  "This is only a macro because qq/set-path! wasn't being found."
  []
 `(do (when-not (fs/exists? "quick-question")
         (println "Need to do some first time setup, please be patient...")
         (shell "git clone https://github.com/escherize/quick-question.git .qq")
         (shell {:dir "./.qq"} "npm install enquirer")
         (println "We're ready to go."))
       (deps/add-deps '{:deps {qq/qq {:local/root "./.qq"}}})
       (require '[qq :as qq])
       (qq/set-path! (str (str (fs/parent *file*)) "/.qq"))))

(install!)

;; optionally:
(when (:question (qq/ask! {:type :confirm
                           :name :question
                           :message (str "Hi! Want to see the tutorial? "
                                         "It guides you through building up some prompts, and then lets you answer them.")}))
  (qq/tutorial))

```

Run it with `bb install.clj`

Or, if you are on a mac, copy the file contents above, and run `pbpaste > install.clj && bb install.clj`

### Installation Demo:
[![asciicast](https://asciinema.org/a/w0qaYpsSofJa2FQp21FlgYith.png)](https://asciinema.org/a/w0qaYpsSofJa2FQp21FlgYith)

## Usage

The main function to use thing, is [qq/ask!](https://github.com/escherize/quick-question/blob/master/qq.clj#L19). You can pass it 1 single map, or a collection of maps. These maps describe questions that enquirer.js will quickly ask you. Every question has a name, and that name will become a key in a map returned by `ask!`.

## Self Documentation

See: [qq/tutorial](https://github.com/escherize/quick-question/blob/master/qq.clj#L125)

## FAQ

> Why not just use edn.deps like a git sha or what not?

This project needs things from npm (sorry), so we actually npm install enquirer on the first run. 
