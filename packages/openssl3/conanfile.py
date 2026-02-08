from os.path import join

from conan import ConanFile
from conan.tools.files import copy


class Openssl3Conan(ConanFile):
    options = {
        "shared": [True, False],
    }

    def requirements(self):
        self.requires("openssl/" + str(self.version))

    def generate(self):
        for _, dep in self.dependencies.host.items():
            includedir = dep.cpp_info.includedirs
            libdir = dep.cpp_info.libdirs
            bindir = dep.cpp_info.bindirs
            for sources, destination in [
                (includedir, "include"),
                (libdir, "lib"),
                (bindir, "bin")
            ]:
                for source in sources:
                    copy(self, "**.*", source, join(self.build_folder, destination))
