# peep

peep is a work-in-progress project to classify and tag species (and other) information automatically from free text
input on social media websites 🦊👀🐰.

For example, on [barq.app](https://barq.app) there's a free text field for species which could result in:
* `Lop-eared bunny` -> `species:rabbit`, `family:leporidae`, `order:lagomorpha`, `characterisation:lop-eared`
* `Arctic fox` -> `species:arctic fox`, `species:fox`, `species:vulpes lagopus`, `genus:vulpes`, `family:canidae`
* `Blue sparkleyote` -> `species:coyote`, `species:canis latrans`, `genus:canis`, `family:canidae`, `color:blue`, `color:sparkle`

This is useful for data analysis, including monitoring species popularity over time, whilst also letting users express
themselves freely.

If this sounds interesting to you, please star the repo - thanks ⭐️!

## How does it work

The initial plan is to do this by:
* Tidying/normalising input data
* Using some form of natural language processing
  * Probably with some cultural additions that tend not to appear in popular language
* Nearest-matching to a configurable taxonomy, using an appropriate algorithm

Some other goals / non-goals:
* Take some samples from an existing dataset to do classification performance measurement
* Try combining different methods and benchmark each
* Make an API
  * Including batch classification
* Not interested in ML/AI classification to begin with