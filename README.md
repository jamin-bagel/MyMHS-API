#MyMHS API
MyMHS API is an ongoing REST API project that I am working on. Its purpose is to provide a reliable JSON representation of student data parsed from MTSD's student manager and gradebook, Genesis.

###Planned Features
- A *nearly* full-coverage parser for Genesis that can accurately return a JSON representation for most pages on Genesis, especially the student summary, assignment list and gradebook
- Various utility endpoints, including GPA calculation, grades over time (graphable) and more
- A pathfinder (returning an ordered list of nodes) to help students get around MHS based on their schedules
- (Eventually) Support for LMS through MHS students (Genesis varies between grades in slight ways that can throw off a parser)